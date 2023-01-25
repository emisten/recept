package se.emilius.receptdatabasen;

import java.sql.*;
import java.util.Scanner;

public class Application {


    public static void main(String[] args) throws SQLException {

        connect();
        boolean quit = false;
        while (!quit) {
            printMenu();
            int action = scanner.nextInt();
            scanner.nextLine();

            switch (action) {
                case 7 -> {
                    System.out.println(" Avslutar!");
                    quit = true;
                }
                case 1 -> addRecipe();
                case 2 -> updateRecipe();
                case 3 -> showAllRecipe();
                case 4 -> deleteRecipe();
                case 5 -> countRecipes();
                case 6 -> searchRecipe();


            }
        }

    }

    private static Scanner scanner = new Scanner(System.in);

    private static Connection connect() {

        String url = "jdbc:sqLite:C:\\Users\\bigchillin\\OneDrive\\Skrivbord\\db\\receptdatabasen.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    private static void printMenu() {
        System.out.println();
        System.out.println();
        System.out.println("Välj ett alternativ");
        System.out.println("========");
        System.out.println("1. Lägg till ett recept");
        System.out.println("2. Uppdatera ett recept");
        System.out.println("3. Visa alla recept");
        System.out.println("4. Ta bort ett recept");
        System.out.println("5. Visa antal recept");
        System.out.println("6. Sök efter recept");
        System.out.println("7. Avsluta programmet");
    }

    private static void addRecipe() {


        System.out.println();
        System.out.println();
        System.out.println("Namnge ditt recept: ");
        String namn = scanner.nextLine();


        System.out.println("Beskriv hur man lagar receptet: ");
        String instruktion = scanner.nextLine();
        int recipeId = 0;
        String recipeSql = "INSERT INTO recept(namn, instruktion) VALUES(?, ?)";

        try {
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(recipeSql);
            pstmt.setString(1, namn);
            pstmt.setString(2, instruktion);
            pstmt.executeUpdate();
            ResultSet savedRecipe = pstmt.getGeneratedKeys();
            recipeId = savedRecipe.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }


        String ingrediensNamn = null;
        do {
            System.out.println("Ange din ingrediens: ");
            ingrediensNamn = scanner.nextLine();

            if (!ingrediensNamn.isEmpty()) {
                System.out.println("Ange kvantitet av ingrediensen: ");

                String kvantitet = scanner.nextLine();

                String ingredientSql = "INSERT INTO ingrediens(namn, kvantitet, receptid) VALUES (?,?,?)";

                try {
                    Connection conn = connect();
                    PreparedStatement pstmt = conn.prepareStatement(ingredientSql);
                    pstmt.setString(1, ingrediensNamn);
                    pstmt.setString(2, kvantitet);
                    pstmt.setInt(3, recipeId);
                    pstmt.executeUpdate();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        } while (!ingrediensNamn.isEmpty());

        System.out.println("Sparat recept!");


    }

    private static void deleteRecipe() {
        System.out.println("Skriv in id på det receptet som du vill ta bort: ");
        int inputRecipeId = scanner.nextInt();
        delete(inputRecipeId);
        scanner.nextLine();
    }

    private static void delete(int id) {
        String sql = "DELETE FROM recept WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Receptet har tagits bort!");


        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void showAllRecipe() {
        String sql = "SELECT * FROM recept";

        try {
            Connection conn = connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                System.out.println(rs.getInt("id") + "\t" +
                        rs.getString("namn") + "\t");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Skriv in ID:et på det receptet du vill se, eller tryck enter för att gå tillbaka till menyn: ");
        String input = scanner.nextLine();

        if (input.isEmpty()) {
            return;
        }
        Recipe recipe = fetchRecipe(input);
        System.out.println(recipe.getName());
        System.out.println("Ingredienser: ");
        for (Ingredient ingredient : recipe.getIngredients()) {
            System.out.println(" * " + ingredient.getName() + ", " + ingredient.getQuantity());
        }
        System.out.println("Instruktion: ");
        System.out.println(recipe.getInstructions());

        scanner.nextLine();
    }


    private static void updateRecipe() {
        System.out.println("Välj receptet du vill ändra på genom att skriva in receptID");
        String receptId = scanner.nextLine();

        Recipe recipe = fetchRecipe(receptId);


        System.out.println(recipe.getName());
        System.out.println("Instruktion: ");
        System.out.println(recipe.getInstructions());


        System.out.println("Mata in den nya instruktionen: ");
        String instruction = scanner.nextLine();
        update(Integer.parseInt(receptId), instruction);
        scanner.nextLine();


    }

    private static void update(int id, String instruction) {
        String sql = "UPDATE recept SET instruktion = ? WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, instruction);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            System.out.println("Receptet har uppdaterats!");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    private static Recipe fetchRecipe(String recipeID) {

        Recipe recipe = new Recipe();
        int recipeId = Integer.parseInt(recipeID);
        String recipeSql = "SELECT * FROM recept WHERE id = ?";
        try {
            Connection conn = connect();
            PreparedStatement stmt = conn.prepareStatement(recipeSql);
            stmt.setInt(1, recipeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                recipe.setId(recipeId);
                recipe.setName(rs.getString("namn"));
                recipe.setInstructions(rs.getString("instruktion"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String ingredientSql = "SELECT ingrediens.receptid, ingrediens.namn, ingrediens.kvantitet FROM ingrediens" +
                "JOIN recept ON recept.id = ingrediens.receptid" +
                "WHERE recept.id = ?";
        try {
            Connection conn = connect();
            PreparedStatement stmt = conn.prepareStatement(ingredientSql);
            stmt.setInt(1, recipeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setRecipeId(recipeId);
                ingredient.setName(rs.getString("namn"));
                ingredient.setQuantity(rs.getString("kvantitet"));
                recipe.getIngredients().add(ingredient);
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return recipe;

    }


    private static void countRecipes() throws SQLException {

        Connection conn = connect();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT (*)AS numberOfRecipes FROM recept");
        rs.next();
        Integer numberOfRecipes = rs.getInt("numberOfRecipes");
        System.out.println("Det finns " + numberOfRecipes + " recept!");
        scanner.nextLine();
    }


    private static void searchRecipe() {


        System.out.println("Sök efter ett recept");
        String recipeName = scanner.nextLine();

        if (recipeName.isEmpty()) {
            return;
        } else {
            Recipe recipe = new Recipe();
            String recipeSql = "SELECT * FROM recept WHERE lower(namn) LIKE ?";
            try {
                Connection conn = connect();
                PreparedStatement stmt = conn.prepareStatement(recipeSql);
                stmt.setString(1, "%" + recipeName.toLowerCase() + "%");
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    recipe.setId(rs.getInt("id"));
                    recipe.setName(rs.getString("namn"));
                    recipe.setInstructions(rs.getString("instruktion"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            String ingredientSql = "SELECT ingrediens.receptid, ingrediens.namn, ingrediens.kvantitet FROM ingrediens" +
                    "JOIN recept ON recept.id = ingrediens.receptid" +
                    "WHERE recept.id = ?";
            try {
                Connection conn = connect();
                PreparedStatement stmt = conn.prepareStatement(ingredientSql);
                stmt.setInt(1, recipe.getId());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setRecipeId(recipe.getId());
                    ingredient.setName(rs.getString("namn"));
                    ingredient.setQuantity(rs.getString("kvantitet"));
                    recipe.getIngredients().add(ingredient);
                }
            } catch (SQLException e) {
                e.printStackTrace();

            }
            System.out.println(recipe.getName());
            System.out.println("Ingredienser: ");
            for (Ingredient ingredient : recipe.getIngredients()) {
                System.out.println(" * " + ingredient.getName() + ", " + ingredient.getQuantity());
            }
            System.out.println("Instruktion: ");
            System.out.println(recipe.getInstructions());

        }


    }
}




