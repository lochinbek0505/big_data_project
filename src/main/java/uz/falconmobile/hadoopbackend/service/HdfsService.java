package uz.falconmobile.hadoopbackend.service;



import org.apache.hadoop.fs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.falconmobile.hadoopbackend.model.FileInfo;

import java.io.*;
import java.util.*;

@Service
public class HdfsService {

    @Autowired
    private FileSystem fileSystem;

    // Fayl yuklash
    public String uploadFile(MultipartFile file, String hdfsDirectory) throws IOException {
        String fileName = file.getOriginalFilename();
        String hdfsPath = hdfsDirectory + "/" + fileName;
        Path path = new Path(hdfsPath);

        // Agar fayl mavjud bo'lsa, o'chirish
        if (fileSystem.exists(path)) {
            fileSystem.delete(path, false);
        }

        try (InputStream inputStream = file.getInputStream();
             FSDataOutputStream outputStream = fileSystem.create(path)) {

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();
        }

        return hdfsPath;
    }

    // Fayllar ro'yxati
    public List<FileInfo> listFiles(String hdfsPath) throws IOException {
        List<FileInfo> fileInfoList = new ArrayList<>();
        Path path = new Path(hdfsPath);

        if (!fileSystem.exists(path)) {
            return fileInfoList;
        }

        FileStatus[] fileStatuses = fileSystem.listStatus(path);

        for (FileStatus status : fileStatuses) {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setName(status.getPath().getName());
            fileInfo.setPath(status.getPath().toString());
            fileInfo.setSize(status.getLen());
            fileInfo.setType(status.isDirectory() ? "directory" : "file");
            fileInfo.setModificationTime(status.getModificationTime());
            fileInfo.setReplication(status.getReplication());

            fileInfoList.add(fileInfo);
        }

        return fileInfoList;
    }

    // Faylni o'qish
    public String readFile(String hdfsPath) throws IOException {
        Path path = new Path(hdfsPath);

        if (!fileSystem.exists(path)) {
            throw new FileNotFoundException("Fayl topilmadi: " + hdfsPath);
        }

        StringBuilder content = new StringBuilder();

        try (FSDataInputStream inputStream = fileSystem.open(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }

        return content.toString();
    }

    // Faylni o'chirish
    public boolean deleteFile(String hdfsPath) throws IOException {
        Path path = new Path(hdfsPath);
        return fileSystem.delete(path, true);
    }

    // Katalog yaratish
    public boolean createDirectory(String hdfsPath) throws IOException {
        Path path = new Path(hdfsPath);
        return fileSystem.mkdirs(path);
    }

    // Fayl ma'lumotlarini olish
    public FileInfo getFileInfo(String hdfsPath) throws IOException {
        Path path = new Path(hdfsPath);

        if (!fileSystem.exists(path)) {
            throw new FileNotFoundException("Fayl topilmadi: " + hdfsPath);
        }

        FileStatus status = fileSystem.getFileStatus(path);

        FileInfo fileInfo = new FileInfo();
        fileInfo.setName(status.getPath().getName());
        fileInfo.setPath(status.getPath().toString());
        fileInfo.setSize(status.getLen());
        fileInfo.setType(status.isDirectory() ? "directory" : "file");
        fileInfo.setModificationTime(status.getModificationTime());
        fileInfo.setReplication(status.getReplication());

        return fileInfo;
    }
}