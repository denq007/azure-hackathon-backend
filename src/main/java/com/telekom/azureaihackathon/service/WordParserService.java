package com.telekom.azureaihackathon.service;

import com.telekom.azureaihackathon.model.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.telekom.azureaihackathon.model.StaticVariables.businessRequirements;

@Service
@RequiredArgsConstructor
@Slf4j
public class WordParserService {

    public void processFiles(MultipartFile file) throws IOException {
        if (isArchive(file.getOriginalFilename())) {
            unzipMultipartFile(file);
            readAllFiles();
        } else if (isDocFile(file.getOriginalFilename())) {
            getBusinessRequirements(file);
        } else {
            throw new IllegalArgumentException("Unsupported file type");
        }
    }

    private boolean isArchive(String fileName) {
        String extension = FilenameUtils.getExtension(fileName);
        return "zip".equalsIgnoreCase(extension) || "tar".equalsIgnoreCase(extension);
    }

    private boolean isDocFile(String fileName) {
        String extension = FilenameUtils.getExtension(fileName);
        return "doc".equalsIgnoreCase(extension);
    }

    public void getBusinessRequirements(final MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        FileInputStream fis = new FileInputStream(convFile.getAbsolutePath());
        HWPFDocument document = new HWPFDocument(fis);
        final var initText = document.getRange().text();
        String cleanString = initText.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]+", " ");
        cleanString = cleanString.replaceAll("[^\\x20-\\x7E]", " ");
        final var preparedParagraph = deleteTextOutsideKeywords(cleanString);
        businessRequirements.add(preparedParagraph + " ");
        convFile.delete();
    }

    public void getBusinessRequirementsFromXML(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        String xmlContent = String.join(System.lineSeparator(), lines);
        final String preparedParagraph = deleteTextOutsideKeywordsFromXml(xmlContent);
        businessRequirements.add(preparedParagraph + " ");
    }

    public String deleteTextOutsideKeywordsFromXml(String xmlContent) {
        StringBuilder extractedTextBuilder = new StringBuilder();

        // Pattern for Acceptance criteria paragraphs
        Pattern accCriteriaPattern = Pattern.compile("<p><b>Acceptance criteria</b></p>(.*?)<\\/p>", Pattern.DOTALL);
        // Pattern for comments
        Pattern commentsPattern = Pattern.compile("<comment>(.*?)<\\/comment>", Pattern.DOTALL);
        Pattern descriptionPattern = Pattern.compile("<p><b>User Story</b></p>(.*?)<\\/p>", Pattern.DOTALL); //TODO: change to description
        // TODO: cleanup html tags
        // Find and add Acceptance criteria text
        Matcher accCriteriaMatcher = accCriteriaPattern.matcher(xmlContent);
        while (accCriteriaMatcher.find()) {
            extractedTextBuilder.append(accCriteriaMatcher.group(1).trim()).append(" ");
        }

        // Find and add comment text
        Matcher commentsMatcher = commentsPattern.matcher(xmlContent);
        while (commentsMatcher.find()) {
            extractedTextBuilder.append(commentsMatcher.group(1).trim()).append(" ");
        }

        //Find and add description text
        Matcher descriptionMatcher = descriptionPattern.matcher(xmlContent);
        while (descriptionMatcher.find()) {
            extractedTextBuilder.append(descriptionMatcher.group(1).trim()).append(" ");
        }

        // Convert to string, cleanup all html tags and return
        return cleanHtmlTags(extractedTextBuilder.toString());
    }

    private String cleanHtmlTags(final String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("<[^>]*>", "");
    }

    private String deleteTextOutsideKeywords(String text) {
        String startKeyword = "user story";
        int startIndex = text.toLowerCase().indexOf(startKeyword);
        int endIndex = text.toLowerCase().indexOf("comments");

        // Check if both keywords exist
        if (startIndex != -1 && endIndex != -1) {
            // Add the length of the startKeyword to the startIndex to not include the startKeyword itself
            startIndex += startKeyword.length();
            return text.substring(startIndex, endIndex).trim();
        }
        throw new RuntimeException("Keywords are not found in word file");
    }

    private void readAllFiles() throws IOException {
        String directoryPath = "src/main/resources/docks";
        File dir = new File(directoryPath);

        if (dir.exists() && dir.isDirectory()) {
            File[] fileList = dir.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (file.isFile()) {
                        getBusinessRequirementsFromXML(file);
                        file.delete();
                    }
                }
            }
        } else {
            System.out.println("The specified directory does not exist or is not a directory.");
        }
    }


    public static void unzipMultipartFile(MultipartFile zipFile) {
        String destDirPath = "src/main/resources/docks";
        File destDir = new File(destDirPath);

        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        try (InputStream inputStream = zipFile.getInputStream();
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                File fileToUnzip = new File(destDir, entry.getName());

                if (entry.isDirectory()) {
                    fileToUnzip.mkdirs();
                } else {
                    File parent = fileToUnzip.getParentFile();
                    if (parent != null) {
                        parent.mkdirs();
                    }

                    Files.copy(zipInputStream, fileToUnzip.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                zipInputStream.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ChatResponse getUml(ChatResponse simpleAnswerChatGpt) {
        var response=simpleAnswerChatGpt.response();
        int startIndex = response.indexOf("@startuml");
        int endIndex=response.indexOf("@enduml");
        String umlSubstring=null;
        if (startIndex != -1 && endIndex != -1) {
            umlSubstring = response.substring(startIndex, endIndex + "@enduml".length());
          log.info("uml diagrama:"+umlSubstring);
        } else {
            System.out.println("Markers not found in the string.");
        }
        return new ChatResponse(umlSubstring,simpleAnswerChatGpt.tokenCount());
    }
}
