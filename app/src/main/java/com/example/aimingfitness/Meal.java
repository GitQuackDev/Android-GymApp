package com.example.aimingfitness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Meal {
    private String mealId;
    private String userId; // Added userId field
    private String name;
    private String description;
    private String imageUrl;
    private double calories;
    private double protein;
    private double carbs;
    private double fats;
    private List<String> ingredients;
    private String recipe; // Instructions or steps
    private String mealType; // e.g., Breakfast, Lunch, Dinner, Snack

    // Required empty public constructor for Firestore deserialization
    public Meal() {}

    public Meal(String userId, String name, String description, String imageUrl, double calories, double protein, double carbs, double fats, List<String> ingredients, String recipe, String mealType) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fats = fats;
        this.ingredients = ingredients;
        this.recipe = recipe;
        this.mealType = mealType;
    }

    // Getters
    public String getMealId() { return mealId; }
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public double getCalories() { return calories; }
    public double getProtein() { return protein; }
    public double getCarbs() { return carbs; }
    public double getFats() { return fats; }
    public List<String> getIngredients() { return ingredients; }
    public String getRecipe() { return recipe; }
    public String getMealType() { return mealType; }

    // Setters
    public void setMealId(String mealId) { this.mealId = mealId; } // For Firestore document ID
    public void setUserId(String userId) { this.userId = userId; } // Added setter for userId
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCalories(double calories) { this.calories = calories; }
    public void setProtein(double protein) { this.protein = protein; }
    public void setCarbs(double carbs) { this.carbs = carbs; }
    public void setFats(double fats) { this.fats = fats; }    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    public void setRecipe(String recipe) { this.recipe = recipe; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    /**
     * Creates a sample list of meals for demonstration purposes
     */
    public static List<Meal> getSampleMeals() {
        List<Meal> meals = new ArrayList<>();

        // Breakfast meals
        meals.add(new Meal(
            "sample_user",
            "Greek Yogurt Parfait",
            "Protein-rich parfait with Greek yogurt, berries, and granola",
            "yogurt_parfait_image",
            285,
            20.5,
            35.0,
            8.5,
            Arrays.asList("1 cup Greek yogurt", "1/2 cup mixed berries", "1/4 cup granola", "1 tbsp honey"),
            "1. Layer Greek yogurt in a bowl. 2. Add mixed berries. 3. Top with granola and drizzle with honey.",
            "Breakfast"
        ));

        meals.add(new Meal(
            "sample_user",
            "Avocado Toast with Eggs",
            "Nutritious breakfast with whole grain toast, avocado, and scrambled eggs",
            "avocado_toast_image",
            425,
            18.0,
            28.0,
            28.0,
            Arrays.asList("2 slices whole grain bread", "1 ripe avocado", "2 large eggs", "Salt", "Black pepper", "Olive oil"),
            "1. Toast the bread. 2. Mash avocado with salt and pepper. 3. Scramble eggs in olive oil. 4. Spread avocado on toast and top with eggs.",
            "Breakfast"
        ));

        meals.add(new Meal(
            "sample_user",
            "Protein Smoothie Bowl",
            "Thick smoothie bowl topped with nuts, seeds, and fresh fruit",
            "smoothie_bowl_image",
            320,
            25.0,
            42.0,
            8.0,
            Arrays.asList("1 scoop protein powder", "1 frozen banana", "1/2 cup frozen berries", "1/4 cup almond milk", "2 tbsp chia seeds", "1/4 cup sliced almonds"),
            "1. Blend protein powder, banana, berries, and almond milk until thick. 2. Pour into bowl. 3. Top with chia seeds and almonds.",
            "Breakfast"
        ));

        meals.add(new Meal(
            "sample_user",
            "Oatmeal with Banana and Nuts",
            "Hearty oatmeal topped with sliced banana and mixed nuts",
            "oatmeal_image",
            380,
            12.0,
            55.0,
            14.0,
            Arrays.asList("1 cup rolled oats", "1 medium banana", "1/4 cup mixed nuts", "1 tbsp almond butter", "1 tsp cinnamon", "1 cup milk"),
            "1. Cook oats with milk and cinnamon. 2. Slice banana. 3. Top oatmeal with banana, nuts, and almond butter.",
            "Breakfast"
        ));

        meals.add(new Meal(
            "sample_user",
            "Veggie Omelet",
            "Fluffy omelet packed with colorful vegetables and cheese",
            "veggie_omelet_image",
            295,
            22.0,
            8.0,
            20.0,
            Arrays.asList("3 large eggs", "1/4 cup bell peppers", "1/4 cup spinach", "2 tbsp onions", "1/4 cup cheddar cheese", "Olive oil", "Salt", "Pepper"),
            "1. Beat eggs with salt and pepper. 2. Sauté vegetables in olive oil. 3. Pour eggs over vegetables. 4. Add cheese and fold omelet.",
            "Breakfast"
        ));

        // Lunch meals
        meals.add(new Meal(
            "sample_user",
            "Grilled Chicken Salad",
            "Fresh mixed greens with grilled chicken breast and vinaigrette",
            "chicken_salad_image",
            385,
            35.0,
            15.0,
            22.0,
            Arrays.asList("4 oz grilled chicken breast", "2 cups mixed greens", "1/2 cup cherry tomatoes", "1/4 cup cucumber", "2 tbsp olive oil vinaigrette", "1/4 avocado"),
            "1. Grill chicken breast and slice. 2. Toss greens with vegetables. 3. Top with chicken and avocado. 4. Drizzle with vinaigrette.",
            "Lunch"
        ));

        meals.add(new Meal(
            "sample_user",
            "Quinoa Power Bowl",
            "Nutritious bowl with quinoa, roasted vegetables, and tahini dressing",
            "quinoa_bowl_image",
            450,
            16.0,
            58.0,
            18.0,
            Arrays.asList("1 cup cooked quinoa", "1/2 cup roasted sweet potato", "1/2 cup roasted broccoli", "2 tbsp chickpeas", "2 tbsp tahini", "1 tbsp lemon juice", "Mixed greens"),
            "1. Cook quinoa according to package directions. 2. Roast vegetables at 400°F for 25 minutes. 3. Mix tahini with lemon juice. 4. Assemble bowl and drizzle with dressing.",
            "Lunch"
        ));

        meals.add(new Meal(
            "sample_user",
            "Turkey and Hummus Wrap",
            "Whole wheat wrap with lean turkey, hummus, and fresh vegetables",
            "turkey_wrap_image",
            340,
            28.0,
            35.0,
            12.0,
            Arrays.asList("1 whole wheat tortilla", "4 oz sliced turkey", "3 tbsp hummus", "Lettuce", "Tomato", "Cucumber", "Red onion"),
            "1. Spread hummus on tortilla. 2. Layer turkey and vegetables. 3. Roll tightly and cut in half.",
            "Lunch"
        ));

        meals.add(new Meal(
            "sample_user",
            "Asian Lettuce Wraps",
            "Light and flavorful chicken lettuce wraps with Asian-inspired sauce",
            "lettuce_wraps_image",
            275,
            24.0,
            18.0,
            12.0,
            Arrays.asList("4 oz ground chicken", "8 butter lettuce leaves", "1/4 cup water chestnuts", "2 tbsp soy sauce", "1 tbsp sesame oil", "Green onions", "Ginger", "Garlic"),
            "1. Cook ground chicken with garlic and ginger. 2. Add water chestnuts, soy sauce, and sesame oil. 3. Serve in lettuce cups. 4. Garnish with green onions.",
            "Lunch"
        ));

        meals.add(new Meal(
            "sample_user",
            "Mediterranean Bowl",
            "Greek-inspired bowl with falafel, tzatziki, and fresh vegetables",
            "mediterranean_bowl_image",
            420,
            18.0,
            45.0,
            22.0,
            Arrays.asList("3 baked falafel", "1/2 cup brown rice", "3 tbsp tzatziki", "Cherry tomatoes", "Cucumber", "Red onion", "Kalamata olives", "Feta cheese"),
            "1. Prepare brown rice. 2. Bake falafel according to package directions. 3. Arrange vegetables in bowl. 4. Top with falafel, tzatziki, and feta.",
            "Lunch"
        ));

        // Dinner meals
        meals.add(new Meal(
            "sample_user",
            "Baked Salmon with Quinoa",
            "Omega-3 rich salmon with herb-seasoned quinoa and steamed vegetables",
            "salmon_quinoa_image",
            485,
            38.0,
            35.0,
            22.0,
            Arrays.asList("5 oz salmon fillet", "3/4 cup cooked quinoa", "1 cup steamed broccoli", "2 tbsp olive oil", "Lemon", "Dill", "Garlic", "Salt", "Pepper"),
            "1. Season salmon with herbs and bake at 400°F for 15 minutes. 2. Cook quinoa with garlic. 3. Steam broccoli. 4. Serve with lemon wedge.",
            "Dinner"
        ));

        meals.add(new Meal(
            "sample_user",
            "Lean Beef Stir-Fry",
            "Colorful vegetable stir-fry with lean beef strips and brown rice",
            "beef_stirfry_image",
            445,
            32.0,
            38.0,
            18.0,
            Arrays.asList("4 oz lean beef strips", "3/4 cup brown rice", "Mixed stir-fry vegetables", "2 tbsp soy sauce", "1 tbsp sesame oil", "Ginger", "Garlic", "Red pepper flakes"),
            "1. Cook brown rice. 2. Stir-fry beef until cooked through. 3. Add vegetables and seasonings. 4. Serve over rice.",
            "Dinner"
        ));

        meals.add(new Meal(
            "sample_user",
            "Stuffed Bell Peppers",
            "Colorful bell peppers stuffed with ground turkey, rice, and vegetables",
            "stuffed_peppers_image",
            365,
            26.0,
            42.0,
            12.0,
            Arrays.asList("2 large bell peppers", "4 oz ground turkey", "1/2 cup brown rice", "1/4 cup diced tomatoes", "Onion", "Garlic", "Italian herbs", "Mozzarella cheese"),
            "1. Cut tops off peppers and remove seeds. 2. Cook turkey with onions and garlic. 3. Mix with rice, tomatoes, and herbs. 4. Stuff peppers and bake at 375°F for 30 minutes.",
            "Dinner"
        ));

        meals.add(new Meal(
            "sample_user",
            "Grilled Chicken with Sweet Potato",
            "Herb-marinated grilled chicken with roasted sweet potato and green beans",
            "chicken_sweet_potato_image",
            425,
            35.0,
            35.0,
            16.0,
            Arrays.asList("5 oz chicken breast", "1 medium sweet potato", "1 cup green beans", "2 tbsp olive oil", "Rosemary", "Thyme", "Garlic", "Salt", "Pepper"),
            "1. Marinate chicken in herbs and olive oil. 2. Grill chicken for 6-7 minutes per side. 3. Roast sweet potato at 425°F for 25 minutes. 4. Steam green beans.",
            "Dinner"
        ));

        meals.add(new Meal(
            "sample_user",
            "Vegetarian Pasta Primavera",
            "Whole wheat pasta with seasonal vegetables in light olive oil sauce",
            "pasta_primavera_image",
            380,
            14.0,
            58.0,
            14.0,
            Arrays.asList("2 oz whole wheat pasta", "Zucchini", "Yellow squash", "Cherry tomatoes", "Bell peppers", "3 tbsp olive oil", "Garlic", "Basil", "Parmesan cheese"),
            "1. Cook pasta according to package directions. 2. Sauté vegetables in olive oil with garlic. 3. Toss pasta with vegetables and herbs. 4. Top with Parmesan.",
            "Dinner"
        ));

        // Snack meals
        meals.add(new Meal(
            "sample_user",
            "Apple with Almond Butter",
            "Crisp apple slices with natural almond butter for protein",
            "apple_almond_butter_image",
            195,
            6.0,
            24.0,
            10.0,
            Arrays.asList("1 medium apple", "2 tbsp almond butter"),
            "1. Wash and slice apple. 2. Serve with almond butter for dipping.",
            "Snack"
        ));

        meals.add(new Meal(
            "sample_user",
            "Greek Yogurt with Berries",
            "Creamy Greek yogurt topped with fresh mixed berries",
            "yogurt_berries_image",
            140,
            15.0,
            18.0,
            2.0,
            Arrays.asList("3/4 cup Greek yogurt", "1/2 cup mixed berries"),
            "1. Place yogurt in bowl. 2. Top with fresh berries.",
            "Snack"
        ));

        meals.add(new Meal(
            "sample_user",
            "Trail Mix",
            "Energy-dense mix of nuts, seeds, and dried fruit",
            "trail_mix_image",
            220,
            8.0,
            18.0,
            16.0,
            Arrays.asList("1/4 cup mixed nuts", "2 tbsp dried fruit", "1 tbsp pumpkin seeds"),
            "1. Mix all ingredients in a bowl. 2. Store in airtight container for freshness.",
            "Snack"
        ));

        meals.add(new Meal(
            "sample_user",
            "Protein Energy Balls",
            "No-bake energy balls made with protein powder, oats, and nut butter",
            "energy_balls_image",
            185,
            12.0,
            16.0,
            9.0,
            Arrays.asList("1 scoop protein powder", "1/4 cup rolled oats", "2 tbsp peanut butter", "1 tbsp honey", "1 tbsp chia seeds"),
            "1. Mix all ingredients in a bowl. 2. Roll into 6 balls. 3. Refrigerate for 30 minutes before serving.",
            "Snack"
        ));

        meals.add(new Meal(
            "sample_user",
            "Hummus and Veggie Sticks",
            "Fresh cut vegetables with protein-rich hummus dip",
            "hummus_veggies_image",
            155,
            6.0,
            18.0,
            7.0,
            Arrays.asList("3 tbsp hummus", "1 cup mixed vegetable sticks (carrots, celery, bell peppers, cucumber)"),
            "1. Wash and cut vegetables into sticks. 2. Serve with hummus for dipping.",
            "Snack"
        ));

        return meals;
    }
}
