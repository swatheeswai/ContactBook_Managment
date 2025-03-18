package com.main;

import com.viewClass.UserView;

public class Main {
    public static void main(String[] args) {
        UserView userView = new UserView();
        userView.showUserView();
    }
}


//compile code: javac -d bin -cp "lib/postgresql-42.7.5.jar" $(find com -name "*.java")
//run Code:java -cp "bin:lib/postgresql-42.7.5.jar" com.main.Main
