package Talk_with.semogong.service;

import Talk_with.semogong.domain.att.Image;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
@NoArgsConstructor
public class S3Service {

    private AmazonS3 s3Client;

    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;


    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    @PostConstruct
    public void setS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);

        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(this.region)
                .build();
    }

    public Image upload(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String pathName = RandomStringUtils.randomAlphanumeric(16) + fileName;
        System.out.println("bucket = " + bucket);
        System.out.println("pathName = " + pathName);
        System.out.println("file.getInputStream() = " + file.getInputStream());
        s3Client.putObject(new PutObjectRequest(bucket, pathName, file.getInputStream(), null)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        Image image = new Image(fileName,s3Client.getUrl(bucket, pathName).toString());
        return image;
    }
}
