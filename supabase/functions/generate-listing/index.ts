const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

type GenerateRequest = { imageBase64?: string; mimeType?: string };

const fallback = (value: unknown, allowed: string[], defaultValue: string) => {
  const normalized = String(value ?? "").trim();
  return allowed.find((item) => item.toLowerCase() === normalized.toLowerCase()) ?? defaultValue;
};

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });

  try {
    const apiKey = Deno.env.get("gemini_api_key");
    if (!apiKey) throw new Error("Gemini API secret is not configured");
    const body = (await request.json()) as GenerateRequest;
    if (!body.imageBase64) throw new Error("A food image is required");

    const categories = ["Vegetables", "Fruits", "Dairy", "Bakery", "Canned Goods", "Prepared Meals", "Other"];
    const dietaryNeeds = ["Regular", "Vegetarian", "Vegan", "Gluten-Free", "Nut-Free"];
    const prompt = `Analyze this food donation photo and return ONLY valid JSON with these keys:
foodName: concise English food name,
description: friendly English description in 1 or 2 sentences,
category: exactly one of ${categories.join(", ")},
dietaryType: exactly one of ${dietaryNeeds.join(", ")}.
Do not use markdown. Use Other and Regular when unsure.`;

    const geminiResponse = await fetch(
      "https://generativelanguage.googleapis.com/v1beta/interactions",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "x-goog-api-key": apiKey,
        },
        body: JSON.stringify({
          model: "gemini-3.6-flash",
          input: [
            { type: "text", text: prompt },
            {
              type: "image",
              data: body.imageBase64,
              mime_type: body.mimeType || "image/jpeg",
            },
          ],
          response_format: {
            type: "text",
            mime_type: "application/json",
            schema: {
              type: "object",
              properties: {
                foodName: { type: "string" },
                description: { type: "string" },
                category: { type: "string", enum: categories },
                dietaryType: { type: "string", enum: dietaryNeeds },
              },
              required: ["foodName", "description", "category", "dietaryType"],
            },
          },
          generation_config: { thinking_level: "minimal", max_output_tokens: 300 },
          store: false,
        }),
      },
    );
    const geminiJson = await geminiResponse.json();
    if (!geminiResponse.ok) throw new Error(geminiJson?.error?.message || "Gemini request failed");
    const outputStep = geminiJson?.steps?.find((item: { type?: string }) => item.type === "model_output");
    const text = geminiJson?.output_text || outputStep?.content?.find((item: { type?: string }) => item.type === "text")?.text;
    if (!text) throw new Error("Gemini returned no listing suggestion");
    const parsed = JSON.parse(String(text).replace(/^```json\s*|\s*```$/g, "").trim());

    return new Response(JSON.stringify({
      foodName: String(parsed.foodName || "").trim(),
      description: String(parsed.description || "").trim(),
      category: fallback(parsed.category, categories, "Other"),
      dietaryType: fallback(parsed.dietaryType, dietaryNeeds, "Regular"),
    }), { headers: { ...corsHeaders, "Content-Type": "application/json" } });
  } catch (error) {
    return new Response(JSON.stringify({ error: error instanceof Error ? error.message : "Unable to generate listing details" }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }
});
