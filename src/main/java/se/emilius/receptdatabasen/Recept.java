package se.emilius.receptdatabasen;

import java.sql.*;
import java.util.Scanner;

public class Recept {


    public static void main(String[] args) {

        connect();
        boolean quit = false;
        printMenu();
        while (!quit) {
            int action = scanner.nextInt();
            scanner.nextLine();

            switch (action) {
                case 5 -> {
                    System.out.println(" Avslutar!");
                    quit = true;
                }
                case 1 -> addRecipe();
//                case 2 ->
//                case 3 -> update("Bilbo", "Tolkien, J.R.R", 100, 1);
                case 4 -> deleteRecipe();
//                case 5 -> printActions();
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
        System.out.println("5. Avsluta programmet");
    }

    private static void addRecipe() {


        System.out.println();
        System.out.println();
        System.out.println("Skriv in ditt recept: ");

        String instruktion = scanner.nextLine();
        int recipeId = 0;
        //insert into recept...
        String recipeSql = "INSERT INTO recept(instruktion) VALUES(?)";

        try {
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(recipeSql);
            pstmt.setString(1, instruktion);
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

            //hello
            //insert into ingrediens....
        } while (!ingrediensNamn.isEmpty());

        System.out.println("Sparat recept!");


    }

    private static void deleteRecipe() {
        System.out.println("Skriv in id på det receptet som du vill ta bort: ");
        int inputRecipeId = scanner.nextInt();
        delete(inputRecipeId);
        scanner.nextInt();
    }

    private static void delete(int id) {
        String sql = "DELETE FROM recept WHERE recipeid = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("Receptet har tagits bort");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}


