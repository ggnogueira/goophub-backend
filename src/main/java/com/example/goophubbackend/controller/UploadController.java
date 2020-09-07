package com.example.goophubbackend.controller;

import com.complexible.stardog.ext.spring.SnarlTemplate;
import com.example.goophubbackend.utils.FileConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.File;

import java.util.UUID;
import java.util.Map;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Controller
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    SnarlTemplate snarlTemplate;

    public static String uploadDirectory = System.getProperty("user.dir")+"/uploads";
    public Cloudinary cloudinary = new Cloudinary("cloudinary_url");

    @RequestMapping("/upload")
    public String uploadPage(Model model) {
        return "uploadview";
    }

    @RequestMapping(value = "/complexupload", method = RequestMethod.POST)
    @ResponseBody
    public String uploadfile(@RequestParam(value="name") String name, @RequestParam(value="email") String email,
                         @RequestParam(value="organization") String organization, @RequestParam(value="role") String role,
                         @RequestParam(value="goal") String goal, @RequestParam(value="atomics[]") String[] atomicGoals,
                         @RequestParam(value="decomposition") String decomposition, @RequestParam("file")MultipartFile[] files,
                         @RequestParam("image")MultipartFile[] images) {

        String uuid = UUID.randomUUID().toString();
        String url = "";
        FileConverter converter = new FileConverter();
        String result = "";
        try {

            System.out.println("Upload Request:");
            System.out.println("\tName: " + name + "\tEmail: " + email);
            System.out.println("\tOrganization: " + organization + "\tRole: " + role);

            StringBuilder filesNames = new StringBuilder();
            StringBuilder imagesNames = new StringBuilder();
            String goalNames = "";

            if(goal.isEmpty() || (files.length == 0 || files.length > 1)) {
                Exception e = new Exception("Invalid Form");
                throw e;
            }

            int i;
            for (i = 0; i < atomicGoals.length; i++) {
                goalNames += atomicGoals[i] + ",";
            }
            goalNames = goalNames.substring(0, goalNames.length()-1);

            for (MultipartFile file : files) {
                Path fileNamePath = Paths.get(uploadDirectory, file.getOriginalFilename());
                filesNames.append(file.getOriginalFilename() + " ");
                try {
                    Files.write(fileNamePath, file.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("\tGoal: " + goal);
            System.out.println("\tGoal Decomposition: " + decomposition);
            System.out.println("\tAtomic Goals: " + goalNames);
            System.out.println("\tFile: "+ filesNames.toString());

            // Image File
            for (MultipartFile image : images) {
                String extension = image.getOriginalFilename().split("\\.")[image.getOriginalFilename().split("\\.").length - 1];
                String imageName = uuid + "." + extension;
                //System.out.println(imageName);
                Path imageNamePath = Paths.get(uploadDirectory, imageName);
                imagesNames.append(image.getOriginalFilename() + " ");
                try {
                    Files.write(imageNamePath, image.getBytes());
                    Map uploadResult = cloudinary.uploader().upload(new File((uploadDirectory + "/" + imageName)), ObjectUtils.emptyMap());
                    url = uploadResult.get("url").toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("\tImage: "+ imagesNames.toString());


            byte[] fileContent = files[0].getBytes();
            String s = new String(fileContent);

            //System.out.println(s);
            result = converter.convertOWLtoGoopComplex(s, role, goal.replace(" ", "_"), decomposition, goalNames.toString(), uuid);
            Thread.sleep(5000);
            return snarlTemplate.execute(connection -> {
                try{
                	System.out.println("Inserindo arquivo");
                    connection.add().io().file(Paths.get("/home/gabriel/eclipse-workspace/goophub-backend/src/main/resources/temp.rdf"));
                    return "{\"status\" : 200}";
                }
                catch (Exception e) {
                    return "{\"error\" : \"Upload error: " + e.getMessage() + "\"}";
                }
            });
        }
        catch (Exception e) {
            return "{\"error\" : \"Upload error: " + e.getMessage() + "\"}";
        }
    }

    @RequestMapping(value = "/atomicupload", method = RequestMethod.POST)
    @ResponseBody
    public String uploadAtomicFile(@RequestParam(value="name") String name, @RequestParam(value="email") String email,
                             @RequestParam(value="organization") String organization, @RequestParam(value="role") String role,
                             @RequestParam(value="goal") String goal,  @RequestParam("file")MultipartFile[] files,
                             @RequestParam("image")MultipartFile[] images) {

        String uuid = UUID.randomUUID().toString();
        String url = "";
        FileConverter converter = new FileConverter();
        String result = "";
        try {

            System.out.println("Upload Request:");
            System.out.println("\tName: " + name + "\tEmail: " + email);
            System.out.println("\tOrganization: " + organization + "\tRole: " + role);

            if(goal.isEmpty() || (files.length == 0 || files.length > 1)) {
                Exception e = new Exception("Invalid Form");
                throw e;
            }

            StringBuilder filesNames = new StringBuilder();
            StringBuilder imagesNames = new StringBuilder();

            int i;

            // OWL File
            for (MultipartFile file : files) {
                Path fileNamePath = Paths.get(uploadDirectory, file.getOriginalFilename());
                filesNames.append(file.getOriginalFilename() + " ");
                try {
                    Files.write(fileNamePath, file.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("\tGoal: " + goal);
            System.out.println("\tFile: "+ filesNames.toString());
            // Image File
            for (MultipartFile image : images) {
                String extension = image.getOriginalFilename().split("\\.")[image.getOriginalFilename().split("\\.").length - 1];
                String imageName = uuid + "." + extension;
                //System.out.println(imageName);
                Path imageNamePath = Paths.get(uploadDirectory, imageName);
                imagesNames.append(image.getOriginalFilename() + " ");
                try {
                    Files.write(imageNamePath, image.getBytes());
                    Map uploadResult = cloudinary.uploader().upload(new File((uploadDirectory + "/" + imageName)), ObjectUtils.emptyMap());
                    url = uploadResult.get("url").toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("\tImage: "+ imagesNames.toString());

            byte[] fileContent = files[0].getBytes();
            String s = new String(fileContent);

            
            result = converter.convertOWLtoGoopAtomic(s, role, goal.replace(" ", "_"), url);
            Thread.sleep(5000);
            // Add file to DataBase
            return snarlTemplate.execute(connection -> {
                try{
                	System.out.println("Inserindo arquivo");
                    connection.add().io().file(Paths.get("/home/gabriel/eclipse-workspace/goophub-backend/src/main/resources/temp.rdf"));
                    return "{\"status\" : 200}";
                }
                catch (Exception e) {
                	System.out.println("Erro Inserindo aqruivo");
                    return "{\"error\" : \"Upload error: " + e.getMessage() + "\"}";
                }
            });
        }
        catch (Exception e) {
            return "{\"error\" : \"Upload error: " + e.getMessage() + "\"}";
        }
    }
}