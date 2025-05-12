package p8z9.base.helper;

import kd.bos.attachment.util.FileSecurityUtil;
import kd.bos.cache.CacheFactory;
import kd.bos.cache.DistributeSessionlessCache;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.fileservice.BatchDownloadRequest;
import kd.bos.fileservice.FileServiceFactory;
import kd.bos.form.control.UrlUtil;
import kd.bos.form.operate.BatchDownloadAttachment;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class FileUtil {

    private static final Log logger = LogFactory.getLog(FileUtil.class);

    public static final int ZIP_CACHE_TIME = 1800;

    /**
     * 附件压缩下载
     *
     * @param attachTree 附件树
     * @return 压缩包临时下载地址
     * @see BatchDownloadAttachment#createZip(List, String, String)
     */
    public static String getZipTempUrl(AttachTree attachTree) {
        if (!attachTree.isDir()) {
            attachTree = AttachTree.dir(attachTree.getName()).add(attachTree);
        }
        String fileName = attachTree.getName() + ".zip";
        BatchDownloadRequest attachReq = new BatchDownloadRequest(fileName);
        ArrayList<BatchDownloadRequest.Dir> dirs = new ArrayList<>();
        ArrayList<BatchDownloadRequest.File> dirFiles = new ArrayList<>();
        attachTree2Dir(attachTree, dirs, dirFiles);
        attachReq.setDirs(dirs.toArray(new BatchDownloadRequest.Dir[0]));
        attachReq.setFiles(dirFiles.toArray(new BatchDownloadRequest.File[0]));

        String urlResult;
        OutputStream fout = null;
        InputStream fin = null;
        File temp = null;
        try {
            String filename = "BatchDownloadAttachment" +
                    System.currentTimeMillis() + UUID.randomUUID().toString().replaceAll("-", "");
            temp = File.createTempFile(filename, ".zip");
            fout = Files.newOutputStream(temp.toPath());
            FileServiceFactory.getAttachmentFileService().batchDownload(attachReq, fout, null);
            fin = Files.newInputStream(temp.toPath());
            urlResult = CacheFactory.getCommonCacheFactory().getTempFileCache()
                    .saveAsFullUrl(attachReq.getFileName(), fin, ZIP_CACHE_TIME); // 保留30分钟
        } catch (Exception e) {
            logger.error(String.format("getZipTempUrl err: %s", e.getMessage()), e);
            return null;
        } finally {
            FileSecurityUtil.safeClose(fout);
            FileSecurityUtil.safeClose(fin);
            FileSecurityUtil.safeDeleteFile(temp);
        }

        return urlResult;
    }

    /**
     * 将附件树结构转换为目录结构
     * 此方法递归地处理附件树，将每个节点根据其是否为目录，分配到相应的目录或文件列表中
     *
     * @param attachTree  附件树的根节点，表示一个文件或目录及其所有子项
     * @param dirList     用于收集目录信息的列表，每个目录项包含目录名和其下的文件及子目录
     * @param dirFileList 用于收集文件信息的列表，每个文件项包含文件名和其URL路径
     */
    private static void attachTree2Dir(AttachTree attachTree, List<BatchDownloadRequest.Dir> dirList, List<BatchDownloadRequest.File> dirFileList) {
        // 检查输入参数的有效性，如果任一参数为null，则直接返回，不进行任何处理
        if (attachTree == null || dirList == null || dirFileList == null) {
            return;
        }

        // 处理文件的情况
        if (!attachTree.isDir()) {
            // 获取文件名和URL路径，并创建一个新的文件对象，然后添加到文件列表中
            String fileName = attachTree.getName();
            String url = attachTree.getPath();
            dirFileList.add(new BatchDownloadRequest.File(fileName, url));
            return;
        }

        // 处理目录的情况
        // 创建一个新的目录对象，并准备收集该目录下的所有文件和子目录
        BatchDownloadRequest.Dir dir = new BatchDownloadRequest.Dir(attachTree.getName());
        List<AttachTree> items = attachTree.getItems();
        final List<BatchDownloadRequest.Dir> dirs = new ArrayList<>();
        final List<BatchDownloadRequest.File> files = new ArrayList<>();

        // 递归处理目录下的每个项
        for (AttachTree item : items) {
            attachTree2Dir(item, dirs, files);
        }

        // 将收集到的文件和子目录设置到当前目录对象中，并将当前目录添加到目录列表中
        dir.setFiles(files.toArray(new BatchDownloadRequest.File[0]));
        dir.setDirs(dirs.toArray(new BatchDownloadRequest.Dir[0]));
        dirList.add(dir);
    }


    /**
     * 追加临时文件权限控制
     *
     * @param url       临时文件url
     * @param times     缓存时间
     * @param entityNum 实体编码
     * @param appId     应用id
     */
    public static void addTempPermCheck(String url, int times, String entityNum, String appId) {
        if (StringUtils.isBlank(url) || !url.contains("tempfile/download.do")) {
            return;
        }
        Map<String, Object> map = new HashMap<>(3);
        map.put("entityNum", entityNum); // 实体编码
        map.put("appId", appId); // 应用id
        map.put("permItem", "2NJ5XVVCMBCL"); // 权限项id - 文件下载
        String id = UrlUtil.getParam(url, "id");
        if (StringUtils.isBlank(id)) {
            return;
        }
        DistributeSessionlessCache cache = CacheFactory.getCommonCacheFactory().getDistributeSessionlessCache("customRegion");
        cache.put("TempFileCheckId:" + id, SerializationUtils.toJsonString(map), times);
    }

    public static class AttachTree {
        boolean dir; // 是否文件夹
        List<AttachTree> items = new ArrayList<>(0); // 文件夹内容
        String name; // 文件/文件夹 名称
        String path; // 文件路径
        long filesize; // 文件大小

        public static AttachTree dir() {
            return new AttachTree(true);
        }

        public static AttachTree dir(String name) {
            AttachTree tree = new AttachTree(true);
            tree.setName(name);
            return tree;
        }

        public static AttachTree file() {
            return new AttachTree(false);
        }

        public static AttachTree file(String name, String url) {
            AttachTree tree = new AttachTree(false);
            tree.setName(name);
            tree.setPath(url);
            return tree;
        }

        public AttachTree(boolean isDir) {
            this.dir = isDir;
        }

        public boolean isDir() {
            return dir;
        }

        public boolean isEmpty() {
            return getItems() == null || getItems().isEmpty();
        }

        public void setDir(boolean dir) {
            this.dir = dir;
        }

        public List<AttachTree> getItems() {
            return items;
        }

        public void setItems(List<AttachTree> items) {
            this.items = items;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public long getFilesize() {
            return filesize;
        }

        public void setFilesize(long filesize) {
            this.filesize = filesize;
        }

        public AttachTree add(AttachTree attachTree) {
            if (attachTree == null) {
                return this;
            }
            getItems().add(attachTree);
            return this;
        }

        public AttachTree addAll(List<AttachTree> attachTreeList) {
            if (attachTreeList == null) {
                return this;
            }
            getItems().addAll(attachTreeList);
            return this;
        }

        public AttachTree addDir(String name) {
            AttachTree dir = AttachTree.dir();
            dir.setName(name);
            getItems().add(dir);
            return dir;
        }

        public AttachTree addAttach(Map<String, Object> attachMap) {
            AttachTree file = AttachTree.file();
            file.setName((String) attachMap.get("name"));
            file.setPath((String) attachMap.get("relativeUrl"));
            getItems().add(file);
            return this;
        }

        public AttachTree addAttach(List<Map<String, Object>> attachMaps) {
            for (Map<String, Object> attachMap : attachMaps) {
                addAttach(attachMap);
            }
            return this;
        }
    }
}
