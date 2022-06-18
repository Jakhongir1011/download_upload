package uz.almas.download_upload.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class AttachmentContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private  byte[] asosiyContent; // ASOSIY CONTENT(MAG'ZI)

    // select * from attachment_content where attachment_id=100
    @OneToOne
    private Attachment attachment; // QAYSI FILE GA TEGISHLI EKANLGI

}
