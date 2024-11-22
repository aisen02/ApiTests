package services;

import assertions.AssertableResponse;
import io.restassured.http.ContentType;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;

import static io.restassured.RestAssured.given;

public class FileService {

    public AssertableResponse downLoadBaseImage(){
        return new AssertableResponse(given().get("/api/files/download").then());
    }

    public AssertableResponse downLoadLastFile(){
        return new AssertableResponse(given().get("/api/files/downloadLastUploaded").then());
    }

    @SneakyThrows
    public AssertableResponse upLoadFile(File file){
        return new AssertableResponse(given().contentType(ContentType.MULTIPART)
                .multiPart("file", "myFile", Files.readAllBytes(file.toPath()))
                .post("/api/files/upload").then());
    }
}
