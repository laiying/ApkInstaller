package com.strod.apkinstaller.compare;

import java.io.File;
import java.util.Comparator;

/**
 * 本地文件名称和文件类型(文件夹在前，文件在后)排序
 * @author laiying
 * @since 2020/10/30
 */
public class LocalFileNameAndTypeComparator implements Comparator<File> {
    @Override
    public int compare(File lhs, File rhs) {
        try {
            /*if (o1.isDirectory() && o2.isFile())
                return -1;
            if (o1.isFile() && o2.isDirectory())
                return 1;
            return o1.getName().compareToIgnoreCase(o2.getName());*/

            boolean l1 = lhs.isDirectory();
            boolean l2 = rhs.isDirectory();
            if (l1 && !l2)
                return -1;
            else if (!l1 && l2)
                return 1;
            else {
                return lhs.getName().compareTo(rhs.getName());
            }
        } catch (IllegalArgumentException e) {
            //todo Comparison method violates its general contract!
        }
        return 0;
    }
}
