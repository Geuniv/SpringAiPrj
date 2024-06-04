package kopo.poly.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class RagConfig {

    @Value("vectorStore.json")
    private String vectorStoreName; // vector store 내용 저장될 파일명

    @Value("classpath:/docs/spring-boot-reference.pdf")
    private Resource pdfResource; // 대상 pdf 파일

    @Bean
    SimpleVectorStore simpleVectorStore(EmbeddingClient embeddingClient) throws IOException {

        // 벡터스토어 사용을 위한 객체 로드
        var simpleVectorStore = new SimpleVectorStore(embeddingClient);
        // 벡터스토어
        var vectorStoreFile = getVectorStoreFile();

        // 벡터스토어 파일이 존재할 경우 저장된 내용을 로드함
        if (vectorStoreFile.exists()) {
            log.info("벡터스토어 파일이 존재하므로 기존 데이터 로드");
            simpleVectorStore.load(vectorStoreFile);
        } else {
            // 벡터스토어 파일이 존재하지 않으면, 커스텀데이터를 읽어와 벡터데이터베이스에 저장
            log.info("벡터스토어 파일이 존재하지 않으므로, pdf 파일 내용을 읽어들이는 작업 시작");
            var config = PdfDocumentReaderConfig.builder()
                    .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder()
                            .withNumberOfBottomTextLinesToDelete(0)
                            .withNumberOfTopTextLinesToDelete(0)
                            .build())
                    .withPagesPerDocument(1)
                    .build();

            // Pdf 파일내용과 Reading 설정 변수를 토대로 PdfReader 객체 생성
            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfResource, config);
            // 읽어들인 내용을 List<Document> 형태로 저장
            List<Document> documents = pdfReader.get();

            // 임베딩 작업을 위해 document를 분할하는 splitter 객체 로드
            TextSplitter textSplitter = new TokenTextSplitter();
            // 읽어들인 내용을 splitter 객체로 분할하여 임베딩에 알맞은 형태로 변환
            List<Document> splitDocuments = textSplitter.apply(documents);

            // splitter에 의해 분할된 객체를 벡터스토어 객체를 통해 저장
            simpleVectorStore.add(splitDocuments);
            simpleVectorStore.save(vectorStoreFile);
        }

        return simpleVectorStore;
    }

    // 벡터스토어 파일에 접근하기 위한 함수
    private File getVectorStoreFile() {
        Path path = Paths.get("src", "main", "resources", "data");
        String absolutePath = path.toFile().getAbsolutePath() + "/" + vectorStoreName;
        return new File(absolutePath);
    }
}
