package uz.almas.download_upload.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import uz.almas.download_upload.Entity.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment,Integer> {


}
