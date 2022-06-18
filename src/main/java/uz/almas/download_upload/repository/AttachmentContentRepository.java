package uz.almas.download_upload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.almas.download_upload.Entity.AttachmentContent;

import java.util.Optional;

public interface AttachmentContentRepository extends JpaRepository<AttachmentContent, Integer> {
    //
    Optional<AttachmentContent> findByAttachmentId(Integer attachment_id);
}
