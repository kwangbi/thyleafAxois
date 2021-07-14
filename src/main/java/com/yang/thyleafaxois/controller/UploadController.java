package com.yang.thyleafaxois.controller;

import com.yang.thyleafaxois.dto.DataDTO;
import com.yang.thyleafaxois.dto.UploadDTO;
import com.yang.thyleafaxois.util.Base64Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;

@Slf4j
@Controller
public class UploadController {

    private final String UPLOAD_DIR = "C:\\factory\\uploads\\";

    @GetMapping("/")
    public String homepage() {
        return "index";
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public String uploadFile(@RequestBody UploadDTO dto,RedirectAttributes attributes) {

        System.out.println("dto : " + dto.toString());

        MultipartFile file = Base64Converter.converter(dto.getFileBase64());



        // check if file is empty
        if (file.isEmpty()) {
            attributes.addFlashAttribute("message", "Please select a file to upload.");
            return "redirect:/";
        }

        // normalize the file path
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        // save the file on the local file system
        try {
            Path path = Paths.get(UPLOAD_DIR + dto.getFilename());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // return success response
        attributes.addFlashAttribute("message", "You successfully uploaded " + fileName + '!');



        return "redirect:/";
    }

    @PostMapping("/api/upload")
    public Map<String, Object> addData(@RequestBody DataDTO data) {

        Map<String, Object> result = new HashMap<>();

        String fileBase64 = data.getFileBase64();

        // 파일이 업로드되지 않았거나 사이즈가 큰 경우를 체크합니다.
        // 사이즈는 일반 바이트에서 1.33을 곱하면 BASE64 사이즈가 대략 나옵니다.

        if(fileBase64 == null || fileBase64.equals("")) {
            result.put("isFileInserted", false);
            result.put("uploadStatus", "FileIsNull");
            return result;
        } else if(fileBase64.length() > 400000) {
            result.put("isFileInserted", false);
            result.put("uploadStatus", "FileIsTooBig");
            return result;
        }

        try {
            String fileName = data.getFileName(); // 파일네임은 서버에서 결정하거나 JSON에서 받아옵니다.

            // 저장할 파일 경로를 지정합니다.
            File file = new File(FileSystemView.getFileSystemView().getHomeDirectory()
                    + "/app/resources/" + fileName);

            // BASE64를 일반 파일로 변환하고 저장합니다.
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] decodedBytes = decoder.decode(fileBase64.getBytes());
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(decodedBytes);
            fileOutputStream.close();

            result.put("isFileInserted", true);
            result.put("uploadStatus", "AllSuccess");

        } catch(IOException e) {
            System.err.println(e);
            result.put("uploadStatus", "FileIsNotUploaded");
            result.put("isTTSInserted", false);
        }

        return result;
    }
}
