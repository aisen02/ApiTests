package tests.swaggertests;

import assertions.AssertableResponse;
import assertions.Conditions;
import assertions.GenericAssertableResponse;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import models.swagger.FullUser;
import models.swagger.Info;
import models.swagger.JwtAuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static assertions.Conditions.hasMessage;
import static assertions.Conditions.hasStatusCode;
import static io.restassured.RestAssured.given;

public class UserTests {

    private static Random random;

    @BeforeAll
    public static void setUp() {
        RestAssured.baseURI = "http://85.192.34.140:8080";
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        random = new Random();
    }

    @Test
    public void positiveRegisterTest(){
        int rundomNumber = Math.abs(random.nextInt(100000));
        FullUser user = FullUser.builder()
                .login("aisen"+rundomNumber)
                .pass("aisimem002").build();

        Info info = given().contentType(ContentType.JSON).body(user)
                .post("/api/signup")
                .then().statusCode(201)
                .extract().jsonPath().getObject("info", Info.class);

        Assertions.assertEquals(info.getMessage(), "User created");

    }

    @Test
    public void negativeRegisterLoginExistsTest(){
        int rundomNumber = Math.abs(random.nextInt(100000));
        FullUser user = FullUser.builder()
                .login("aisen"+rundomNumber)
                .pass("aisimem002").build();

        Info info = given().contentType(ContentType.JSON).body(user)
                .post("/api/signup")
                .then().statusCode(201)
                .extract().jsonPath().getObject("info", Info.class);

        Assertions.assertEquals(info.getMessage(), "User created");

        Info errorInfo = given().contentType(ContentType.JSON).body(user)
                .post("/api/signup")
                .then().statusCode(400)
                .extract().jsonPath().getObject("info", Info.class);

        Assertions.assertEquals(errorInfo.getMessage(), "Login already exist");

    }

    @Test
    public void negativeRegisterNoPasswordTest(){
        int rundomNumber = Math.abs(random.nextInt(100000));
        FullUser user = FullUser.builder()
                .login("aisen"+rundomNumber).build();

        Info info = given().contentType(ContentType.JSON).body(user)
                .post("/api/signup")
                .then().statusCode(400)
                .extract().jsonPath().getObject("info", Info.class);

        /*new AssertableResponse(given().contentType(ContentType.JSON).body(user)
                .post("/api/signup")
                .then()).should(hasMessage("Missing login or password"))
                .should(hasStatusCode(400));*/


        Assertions.assertEquals(info.getMessage(), "Missing login or password");


    }

    @Test
    public void positiveAdminAuthTest(){
        JwtAuthData authData = new JwtAuthData("admin", "admin");

        String token = given().contentType(ContentType.JSON).body(authData)
                .post("/api/login")
                .then().statusCode(200)
                .extract().jsonPath().getString("token");

        Assertions.assertNotNull(token);

    }

    @Test
    public void positiveNewUserAuthTest(){
        int rundomNumber = Math.abs(random.nextInt(100000));
        FullUser user = FullUser.builder()
                .login("aisen"+rundomNumber)
                .pass("aisimem002").build();

        Info info = given().contentType(ContentType.JSON).body(user)
                .post("/api/signup")
                .then().statusCode(201)
                .extract().jsonPath().getObject("info", Info.class);

        Assertions.assertEquals(info.getMessage(), "User created");

        JwtAuthData authData = new JwtAuthData(user.getLogin(), user.getPass());

        String token = given().contentType(ContentType.JSON).body(authData)
                .post("api/login")
                .then().statusCode(200)
                .extract().jsonPath().getString("token");

        Assertions.assertNotNull(token);

    }

    @Test
    public void negativeAuthTest(){
        JwtAuthData authData = new JwtAuthData("user.getLogin()", "user.getPass()");

        given().contentType(ContentType.JSON).body(authData)
                .post("api/login")
                .then().statusCode(401);

    }

    @Test
    public void positiveGetUserInfoTest(){
        JwtAuthData authData = new JwtAuthData("admin", "admin");

        String token = given().contentType(ContentType.JSON).body(authData)
                .post("/api/login")
                .then().statusCode(200)
                .extract().jsonPath().getString("token");

        Assertions.assertNotNull(token);

        given().auth().oauth2(token)
                .get("/api/user")
                .then().statusCode(200);
    }

    @Test
    public void negativeGetUserInfoInvalidJwtTest(){
        JwtAuthData authData = new JwtAuthData("admin", "admin");

        String token = given().contentType(ContentType.JSON).body(authData)
                .post("/api/login")
                .then().statusCode(200)
                .extract().jsonPath().getString("token");

        Assertions.assertNotNull(token);

        given().auth().oauth2("some values " + token)
                .get("/api/user")
                .then().statusCode(401);
    }

    @Test
    public void negativeGetUserInfoWithoutJwtTest(){
        given()
                .get("/api/user")
                .then().statusCode(401);
    }

    @Test
    public void positiveChangeUserPasswordTest(){
        int rundomNumber = Math.abs(random.nextInt(100000));   //Создание пользователя
        FullUser user = FullUser.builder()
                .login("aisen"+rundomNumber)
                .pass("aisimem002").build();

        Info info = given().contentType(ContentType.JSON).body(user) //Регистрация нового пользователя
                .post("/api/signup")
                .then().statusCode(201)
                .extract().jsonPath().getObject("info", Info.class);

        Assertions.assertEquals(info.getMessage(), "User created");

        Map<String, String> password = new HashMap<>(); //Смена пароля
        String updatedPassValue = "newPassUpdated";
        password.put("password", updatedPassValue);

        JwtAuthData authData = new JwtAuthData(user.getLogin(), user.getPass());

        String token = given().contentType(ContentType.JSON).body(authData) //Jwt авторизация
                .post("api/login")
                .then().statusCode(200)
                .extract().jsonPath().getString("token");

        Assertions.assertNotNull(token);

        Info updateInfo = given().auth().oauth2(token) //Обновление пароля
                .contentType(ContentType.JSON).body(password)
                .put("/api/user")
                .then().statusCode(200)
                .extract().jsonPath().getObject("info", Info.class);

        Assertions.assertEquals(updateInfo.getMessage(), "User password successfully changed");

        authData.setPassword(updatedPassValue); //Jwt авторизация
        token = given().contentType(ContentType.JSON).body(authData)
                .post("api/login")
                .then().statusCode(200)
                .extract().jsonPath().getString("token");

        Assertions.assertNotNull(token);

        FullUser userUpdate = given().auth().oauth2(token) //Получение сведении о пользователе
                .get("/api/user")
                .then().statusCode(200)
                .extract().as(FullUser.class);

        Assertions.assertNotEquals(userUpdate.getPass(), user.getPass());
    }

    @Test
    public void negativeChangeAdminPasswordTest(){
        JwtAuthData authData = new JwtAuthData("admin", "admin");

        String token = given().contentType(ContentType.JSON).body(authData) //Jwt авторизация
                .post("api/login")
                .then().statusCode(200)
                .extract().jsonPath().getString("token");

        Assertions.assertNotNull(token);

        Map<String, String> password = new HashMap<>(); //Смена пароля
        String updatedPassValue = "newPassUpdated";
        password.put("password", updatedPassValue);

        Info updateInfo = given().auth().oauth2(token) //Обновление пароля
                .contentType(ContentType.JSON).body(password)
                .put("/api/user")
                .then().statusCode(400)
                .extract().jsonPath().getObject("info", Info.class);

        Assertions.assertEquals(updateInfo.getMessage(), "Cant update base users");

    }

    @Test
    public void negativeDeleteAdminTest(){
        JwtAuthData authData = new JwtAuthData("admin", "admin");

        String token = given().contentType(ContentType.JSON).body(authData) //Jwt авторизация
                .post("api/login")
                .then().statusCode(200)
                .extract().jsonPath().getString("token");

        Assertions.assertNotNull(token);

        Info deleteInfo = given().auth().oauth2(token)
                .delete("/api/user")
                .then().statusCode(400)
                .extract().jsonPath().getObject("info", Info.class);

        Assertions.assertEquals(deleteInfo.getMessage(), "Cant delete base users");
    }

    @Test
    public void positiveDeleteNewUserTest(){
        int rundomNumber = Math.abs(random.nextInt(100000));   //Создание пользователя
        FullUser user = FullUser.builder()
                .login("aisen"+rundomNumber)
                .pass("aisimem002").build();

        Info info = given().contentType(ContentType.JSON).body(user) //Регистрация нового пользователя
                .post("/api/signup")
                .then().statusCode(201)
                .extract().jsonPath().getObject("info", Info.class);

        Assertions.assertEquals(info.getMessage(), "User created");

        JwtAuthData authData = new JwtAuthData(user.getLogin(), user.getPass());

        String token = given().contentType(ContentType.JSON).body(authData) //Jwt авторизация
                .post("api/login")
                .then().statusCode(200)
                .extract().jsonPath().getString("token");

        Assertions.assertNotNull(token);

        Info deleteInfo = given().auth().oauth2(token)
                .delete("/api/user")
                .then().statusCode(200)
                .extract().jsonPath().getObject("info", Info.class);

        Assertions.assertEquals("User successfully deleted", deleteInfo.getMessage());
    }

    @Test
    public void positiveGetAllUserTest(){
        List<String> users = given().get("/api/users")
                .then().statusCode(200)
                .extract().as(new TypeRef<List<String>>() {
                });

        Assertions.assertTrue(users.size()>=3);
    }
}
