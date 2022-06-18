package uz.almas.download_upload.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import uz.almas.download_upload.Entity.Attachment;
import uz.almas.download_upload.Entity.AttachmentContent;
import uz.almas.download_upload.repository.AttachmentContentRepository;
import uz.almas.download_upload.repository.AttachmentRepository;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/attachment")
public class AttachmentController {

    @Autowired
    AttachmentRepository attachmentRepository;

    @Autowired
    AttachmentContentRepository attachmentContentRepository;

   private static final String uploadDirectory="yuklanganlar";


    @PostMapping("/uploadDB")
    // 1.FORM DataNI USHLAB OLISHUMIZ UCHUN(MHSR KK BOLADI). KOP PARTDAN IBORAT REQUEST KK
    public String uploadFileToDB(MultipartHttpServletRequest request) throws IOException {

        // File haqida ma'lumot yozish

        // 2. fileName olamiz bitta reqestda bir nechta file bolishi mumkin shunga Iteratorga orab oladi
        Iterator<String> fileNames = request.getFileNames();
        // 3. File olmoqchimiz. tipi Multipart tipida
        MultipartFile file = request.getFile(fileNames.next());
        // 4. Fileni endi ichini bo'laklarga bo'lamiz ya'ni
        // file teshiramiz

        if (file!=null && file.getSize()!=0){

            String originalFilename = file.getOriginalFilename();
            long size = file.getSize();
            String contentType = file.getContentType();
            // 5. Ana endi Attachmentni saqlasak bo'ladi 1 - tebilga. unga bizga Repository() kk
            // birinchi nega buni saqlaymiz chunki u mustaqil id kk mas inser bola oladi shunga. qaram emas
            Attachment attachment = new Attachment();
            attachment.setFileOriginalName(originalFilename);
            attachment.setSize(size);
            attachment.setContentType(contentType);
            Attachment saveAttachment = attachmentRepository.save(attachment);

            // Fileni CONTENT(byte[]) saqlaymiz
            // 6. AttachmentContentga saqlaymiz
            AttachmentContent attachmentContent = new AttachmentContent();
            // 7. byte fileda keladi shuning uchun getBytes() qilamiz
            attachmentContent.setAsosiyContent(file.getBytes());
            attachmentContent.setAttachment(saveAttachment);
            attachmentContentRepository.save(attachmentContent);
            // 8.File ni Id si Attachment da turibdi
            return "File save. ID :"+saveAttachment.getId();
        }
        return "Error!";
    }



    @PostMapping("/uploadSystem")
    public String uploadFileToSystem(MultipartHttpServletRequest request ) throws IOException {

        Iterator<String> fileNames = request.getFileNames();
        MultipartFile file = request.getFile(fileNames.next());
        if (file != null && file.getSize()!=0){

            String originalFilename = file.getOriginalFilename();

            Attachment attachment = new Attachment();
            attachment.setFileOriginalName(originalFilename);
            attachment.setSize(file.getSize());
            attachment.setContentType(file.getContentType());

            // Uyga.borish
            String[] split = originalFilename.split("\\.");

            String name = UUID.randomUUID().toString() + "." + split[split.length - 1];
            attachment.setName(name);
            attachmentRepository.save(attachment);
            Path path = Paths.get(uploadDirectory+"/"+name);
            Files.copy(file.getInputStream(),path);

            return "File saqlandi ID: "+attachment.getId();
         }

        return "Error!";
    }



    @GetMapping("/getFileDB/{id}")
    // 1. Yo'ldagi ID olishmiz uchun PathVerible.
    // ID NI TOPKANDAN KEYIN RESPONSE GA BERAMIZ
    public void getFile(@PathVariable Integer id, HttpServletResponse response) throws IOException {
        // 2. ATTACHMENT NI OLIB KELAMIZ ID ORQALI OLIB KELDIK. KLENTGA aTTACHMENT KORINADI
        Optional<Attachment> optionalAttachment = attachmentRepository.findById(id);
        // 3. SHUNAQA FILE BORMI
        if (optionalAttachment.isPresent()){
            // 4. FILE NOMI SIZE BOR
            Attachment attachment = optionalAttachment.get();

            Optional<AttachmentContent> contentOptional = attachmentContentRepository.findByAttachmentId(id);
            if (contentOptional.isPresent()){
                AttachmentContent attachmentContent = contentOptional.get();
                // 5. MANA QARA BUNAQA  NARSA OLIB BORISHUNG KK RESPONSE GA. NOMINI BERDIK
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"" + attachment.getFileOriginalName() + "\"");
                // 6. FILE CONTENTINI BERDIK
                response.setContentType(attachment.getContentType());
                // 7. FILE HAQIQIY BYTE NI BERVORISH
                // 1)(attachmentContent.getAsosiyContent()) MALUMOTLAR OMBORIDAN OLINDI
                // 2)(response.getOutputStream()) RESPONSGA STRINGIGA BERIB YBORILYAPTI
                FileCopyUtils.copy(attachmentContent.getAsosiyContent(), response.getOutputStream());
                // COPY BYTE NI NI STRINGGA O'GIRYAPTI
            }
        }
    }



   @GetMapping("/getFileFromSystem/{id}")
    public void getFileFromSystem(@PathVariable Integer id, HttpServletResponse response) throws IOException {
       Optional<Attachment> optionalAttachment = attachmentRepository.findById(id);
       if (optionalAttachment.isPresent()){
           Attachment attachment = optionalAttachment.get();
           response.setHeader("Content-Disposition",
                   "attachment; filename=\"" + attachment.getFileOriginalName() + "\"");
           response.setContentType(attachment.getContentType());
           FileInputStream fileInputStream = new FileInputStream(uploadDirectory + "/" + attachment.getName());
           FileCopyUtils.copy(fileInputStream, response.getOutputStream());
       }
   }


}

