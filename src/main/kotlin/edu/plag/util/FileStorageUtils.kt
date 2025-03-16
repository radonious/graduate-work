package edu.plag.util

class FileStorageUtils {

    companion object {
        /**
         * Определяет путь до папки с загруженными файлами
         * @return путь до папки
         */
        fun getUploadDir(): String {
            return System.getProperty("user.dir") + "/uploads/"
        }
    }
}