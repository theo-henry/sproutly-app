import SwiftUI

struct MealPlanDay: Identifiable, Hashable {
    let id = UUID()
    let day: String
    var breakfast: String
    var lunch: String
    var dinner: String
}

struct MealPlanScreen: View {
    @State private var days: [MealPlanDay] = MealPlanScreen.starterDays
    @State private var isGenerating = false
    @State private var lastGeneratedAt: Date?

    static let starterDays: [MealPlanDay] = [
        .init(day: "Monday",    breakfast: "Oat porridge + berries", lunch: "Chickpea grain bowl",    dinner: "Lentil dahl + rice"),
        .init(day: "Tuesday",   breakfast: "Avocado toast",          lunch: "Tofu noodle soup",       dinner: "Roasted veg traybake"),
        .init(day: "Wednesday", breakfast: "Banana smoothie",        lunch: "Tempeh wrap",            dinner: "Mushroom risotto"),
        .init(day: "Thursday",  breakfast: "Chia pudding",           lunch: "Lemon couscous salad",   dinner: "Black bean chili"),
        .init(day: "Friday",    breakfast: "Granola + plant yogurt", lunch: "Peanut tofu bowl",       dinner: "Veggie pizza night"),
        .init(day: "Saturday",  breakfast: "Pancakes",               lunch: "Big garden salad",       dinner: "Cashew curry"),
        .init(day: "Sunday",    breakfast: "Tofu scramble",          lunch: "Hummus + flatbread",     dinner: "Sunday roast veg"),
    ]

    var body: some View {
        ScreenBackground {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    headerCard
                    ForEach($days) { $day in
                        SproutlyCard {
                            Text(day.day).font(SproutlyType.titleLarge).foregroundColor(Palette.leafMint)
                            mealField("Breakfast", text: $day.breakfast)
                            mealField("Lunch",     text: $day.lunch)
                            mealField("Dinner",    text: $day.dinner)
                        }
                    }
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 24)
            }
        }
        .navigationTitle("Meal plan")
        .navigationBarTitleDisplayMode(.inline)
    }

    private var headerCard: some View {
        SproutlyCard(accent: true) {
            SectionLabel(text: "Week of \(weekStart)")
            Text("Editable plan")
                .font(SproutlyType.titleLarge)
                .foregroundColor(Palette.textPrimary)
            Text(lastGeneratedAt.map { "Last generated \(timeAgo($0))" }
                 ?? "Generate a starter plan tuned to your diet tags, then tweak any cell.")
                .font(SproutlyType.bodyMedium)
                .foregroundColor(Palette.textMuted)
            MintPillButton(label: isGenerating ? "Generating…" : "Generate a Meal Plan",
                           enabled: !isGenerating) {
                Task { await generate() }
            }
        }
        .padding(.top, 8)
    }

    private func mealField(_ label: String, text: Binding<String>) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label).font(SproutlyType.labelLarge).foregroundColor(Palette.textMuted)
            TextField("", text: text)
                .font(SproutlyType.bodyLarge)
                .foregroundColor(Palette.textPrimary)
                .padding(.horizontal, 12).padding(.vertical, 10)
                .background(Palette.bgDeep)
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(Palette.divider, lineWidth: 1))
                .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    private var weekStart: String {
        let cal = Calendar(identifier: .iso8601)
        let now = Date()
        let monday = cal.date(from: cal.dateComponents([.yearForWeekOfYear, .weekOfYear], from: now)) ?? now
        let f = DateFormatter(); f.dateFormat = "MMM d"
        return f.string(from: monday)
    }

    private func timeAgo(_ date: Date) -> String {
        let f = RelativeDateTimeFormatter(); f.unitsStyle = .short
        return f.localizedString(for: date, relativeTo: Date())
    }

    /// Equivalent of the Android "request-meal-plan" Supabase Edge Function
    /// trigger. iOS does not have the Supabase Swift SDK wired here so this is
    /// a local regenerate that swaps in a fresh starter plan. The seam is
    /// intentional: replace with a URLSession call to the Edge Function to
    /// match Android.
    private func generate() async {
        isGenerating = true
        try? await Task.sleep(nanoseconds: 800_000_000)
        days = MealPlanScreen.starterDays.shuffled().map { day in
            var d = day
            let extras = ["+ tahini drizzle", "with crispy chickpeas", "topped with seeds", "served warm", "extra greens"]
            if let pick = extras.randomElement() { d.dinner = "\(d.dinner) \(pick)" }
            return d
        }
        lastGeneratedAt = Date()
        isGenerating = false
    }
}
