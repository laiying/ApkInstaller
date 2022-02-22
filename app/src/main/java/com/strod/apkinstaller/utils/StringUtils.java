package com.strod.apkinstaller.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    private static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";
    public final static String EMPTY = "";

    /**
     * 格式化日期字符串
     *
     * @param date
     * @param pattern
     * @return
     */
    public static String formatDate(Date date, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    /**
     * 格式化日期字符串
     *
     * @param date
     * @return 例如2011-3-24
     */
    public static String formatDate(Date date) {
        return formatDate(date, DEFAULT_DATE_PATTERN);
    }

    /**
     * 获取当前时间 格式为yyyy-MM-dd 例如2011-07-08
     *
     * @return
     */
    public static String getDate() {
        return formatDate(new Date(), DEFAULT_DATE_PATTERN);
    }

    /**
     * 格式化时间 格式为hh:mm:ss 例如：16:06:54
     *
     * @param date
     * @return
     */
    public static String formatTime(Date date) {
        return formatDate(date, DEFAULT_TIME_PATTERN);
    }

    /**
     * 获取当前时间 格式为yyyy-MM-dd hh:mm:ss 例如2011-11-30 16:06:54
     *
     * @return
     */
    public static String getDateTime() {
        return formatDate(new Date(), DEFAULT_DATETIME_PATTERN);
    }

    /**
     * 格式化日期时间字符串
     *
     * @param date
     * @return 例如2011-11-30 16:06:54
     */
    public static String formatDateTime(Date date) {
        return formatDate(date, DEFAULT_DATETIME_PATTERN);
    }

    public static String formatFileSize(long n) {
        if (n < 0) {
            return "";
        }
        String s = "";
        try {
            final DecimalFormat decimalFormat = new DecimalFormat("#.00");
            if (n == 0L) {
                return "0B";
            }
            if (n < 1024L) {
                s = decimalFormat.format((double) n) + "B";
            } else if (n < 1048576L) {
                s = decimalFormat.format(n / 1024.0) + "KB";
            } else if (n < 1073741824L) {
                s = decimalFormat.format(n / 1048576.0) + "MB";
            } else {
                s = decimalFormat.format(n / 1.073741824E9) + "GB";
            }
            return s;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return s;
    }

    public static String join(final ArrayList<String> array, String separator) {
        StringBuffer result = new StringBuffer();
        if (array != null && array.size() > 0) {
            for (String str : array) {
                result.append(str);
                result.append(separator);
            }
            result.delete(result.length() - 1, result.length());
        }
        return result.toString();
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }


    /**
     * 将可能是null的字符串转换成""输出
     *
     * @param str
     * @return
     */
    public static String convertNullString(String str) {
        if (isEmpty(str)) {
            return "";
        }
        return str;
    }

    /**
     * 1. 处理特殊字符 2. 去除后缀名带来的文件浏览器的视图凌乱(特别是图片更需要如此类似处理，否则有的手机打开图库，全是我们的缓存图片)
     *
     * @param url
     * @return
     */
    public static String replaceUrlWithPlus(String url) {
        if (url != null) {
            return url.replaceAll("http://(.)*?/", "").replaceAll("[.:/,%?&=]", "+").replaceAll("[+]+", "+");
        }
        return null;
    }

    /**
     * 验证手机号码(取消正则验证)
     *
     * @param mobiles 手机号码， 移动、联通、电信运营商的号码段
     *                <p>
     *                <b>移动号段：</b>134、135、136、137、138、139、147、150、151、152、157、158、159、182、183、187、188
     *                </p>
     *                <p>
     *                <b>联通号段：</b>130、131、132、145、155、156、185、186
     *                </p>
     *                <p>
     *                <b>电信号段：</b>133、153、180、181、189
     *                </p>
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean isMobileNO(String mobiles) {
//        Pattern p = Pattern.compile("^((13[0-9])|(14[5,7])|(15[^4,\\D])|(18[^4,\\D]))\\d{8}$");
//        Matcher m = p.matcher(mobiles);
//        return m.matches();
        if (TextUtils.isEmpty(mobiles)) {
            return false;
        }
        if (!mobiles.startsWith("1") || 11 != mobiles.length()) {
            return false;
        }
        return true;
    }

    /**
     * 验证固定电话号码
     *
     * @param phone 电话号码，格式：国家（地区）电话代码 + 区号（城市代码） + 电话号码，如：+8602085588447
     *              <p>
     *              <b>国家（地区） 代码 ：</b>标识电话号码的国家（地区）的标准国家（地区）代码。它包含从 0 到 9 的一位或多位数字， 数字之后是空格分隔的国家（地区）代码。
     *              </p>
     *              <p>
     *              <b>区号（城市代码）：</b>这可能包含一个或多个从 0 到 9 的数字，地区或城市代码放在圆括号—— 对不使用地区或城市代码的国家（地区），则省略该组件。
     *              </p>
     *              <p>
     *              <b>电话号码：</b>这包含从 0 到 9 的一个或多个数字
     *              </p>
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkPhone(String phone) {
        String regex = "(\\+\\d+)?(\\d{3,4}\\-?)?\\d{7,8}$";
        return Pattern.matches(regex, phone);
    }

    /**
     * 验证邮箱地址
     *
     * @param
     * @return
     */
    public static boolean isEmail(final CharSequence input) {
        String regex = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        return input != null && input.length() > 0 && Pattern.matches(regex, input);
    }
    /**
     * 验证是否url
     *
     * @param url
     * @return
     */
    public static boolean isURL(String url) {
        Pattern p = Pattern
                .compile("(http://|ftp://|https://|www){0,1}[^\u4e00-\u9fa5\\s]*?\\.(com|net|cn|me|tw|fr)[^\u4e00-\u9fa5\\s]*");
        Matcher m = p.matcher(url);
        return m.matches();
    }

    public static String toMd5(String plainText) {
        StringBuffer buf = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(plainText.getBytes("UTF-8"));
            byte b[] = md.digest();
            int i;
            buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {

                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }

            System.out.println("result: " + buf.toString());// 32位的加密

            System.out.println("result: " + buf.toString().substring(8, 24));// 16位的加密

        } catch (Exception e) {

            e.printStackTrace();
        }
        return buf.toString().toUpperCase();
    }

    // public static String trim(String str) {
    // if (IsUtil.isNullOrEmpty(str)) {
    // return "";
    // }
    // return str.trim();
    // }
    //
    // /** 将中文转换成unicode编码 */
    // public static String gbEncoding(final String gbString) {
    // char[] utfBytes = gbString.toCharArray();
    // String unicodeBytes = "";
    // for (char utfByte : utfBytes) {
    // String hexB = Integer.toHexString(utfByte);
    // if (hexB.length() <= 2) {
    // hexB = "00" + hexB;
    // }
    // unicodeBytes = unicodeBytes + "\\u" + hexB;
    // }
    // //System.out.println("unicodeBytes is: " + unicodeBytes);
    // return unicodeBytes;
    // }
    //
    // /** 将unicode编码转换成中�?*/
    // public static String decodeUnicode(final String dataStr) {
    // int start = 0;
    // int end = 0;
    // final StringBuffer buffer = new StringBuffer();
    // while (start > -1) {
    // end = dataStr.indexOf("\\u", start + 2);
    // String charStr = "";
    // if (end == -1) {
    // charStr = dataStr.substring(start + 2, dataStr.length());
    // } else {
    // charStr = dataStr.substring(start + 2, end);
    // }
    // char letter = (char) Integer.parseInt(charStr, 16); // 16进制parse整形字符串�?
    // buffer.append(new Character(letter).toString());
    // start = end;
    // }
    // //System.out.println(buffer.toString());
    // return buffer.toString();
    // }

    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String removeEmojiUnicode(String str) {
        if (str == null) {
            return null;
        }
        str = str.replaceAll("[^\\u0000-\\uFFFF]", "");
        return str;
    }

    /**
     * 获取字符串的长度，如果有中文，则每个中文字符计为2位
     *
     * @param value 指定的字符串
     * @return 字符串的长度
     */
    public static int lengthContainCN(String value) {
        int valueLength = 0;
        String chinese = "[\u0391-\uFFE5]";
        /* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
        for (int i = 0; i < value.length(); i++) {
            /* 获取一个字符 */
            String temp = value.substring(i, i + 1);
            /* 判断是否为中文字符 */
            if (temp.matches(chinese)) {
                /* 中文字符长度为2 */
                valueLength += 2;
            } else {
                /* 其他字符长度为1 */
                valueLength += 1;
            }
        }
        return valueLength;
    }

    /**
     * 截取含有中文字符串的长度，一个中文算2个字符长度
     *
     * @param value
     * @param len
     * @return
     */
    public static String subStringContainCN(String value, int len) {
        int valueLength = 0;
        String subValue = "";
        String chinese = "[\u0391-\uFFE5]";
        /* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
        for (int i = 0; i < value.length(); i++) {
            /* 获取一个字符 */
            String temp = value.substring(i, i + 1);
            /* 判断是否为中文字符 */
            if (temp.matches(chinese)) {
                /* 中文字符长度为2 */
                valueLength += 2;
            } else {
                /* 其他字符长度为1 */
                valueLength += 1;
            }

            if (valueLength - len == 0) {
                return value.substring(0, i + 1);
            } else if (valueLength - len == 1) {
                return value.substring(0, i);
            }
        }
        return subValue;
    }

    /****
     * 根据传入的数字，如果大于10000,返回以万为单位
     * 保留小数点一位
     * 改方法不会四舍五入
     */
    public static String getMathFloatByNum(long num) {
        String str = "";
        if (num > 9999) {
            String temp = String.valueOf(num);
            String start = temp.substring(0, temp.length() - 4);
            String end = temp.substring(temp.length() - 4, temp.length() - 3);
            str = start;
            if (Integer.parseInt(end) > 0) {
                str += "." + end;
            }
            str += "万";
        } else {
            str = (String.valueOf(num));
        }
        return str;
    }

    /**
     * 将byte[] 转换成字符串
     */
    public static String byte2Hex(byte[] srcBytes) {
        StringBuilder hexRetSB = new StringBuilder();
        for (byte b : srcBytes) {
            String hexString = Integer.toHexString(0x00ff & b);
            hexRetSB.append(hexString.length() == 1 ? 0 : "").append(hexString);
        }
        return hexRetSB.toString();
    }

    /**
     * 将16进制字符串转为转换成字符串
     */
    public static byte[] hex2Bytes(String source) {
        byte[] sourceBytes = new byte[source.length() / 2];
        for (int i = 0; i < sourceBytes.length; i++) {
            sourceBytes[i] = (byte) Integer.parseInt(source.substring(i * 2, i * 2 + 2), 16);
        }
        return sourceBytes;
    }

    /**
     * 给url追加参数
     *
     * @param url
     * @param paramName
     * @param params
     * @return
     */
    public static String appendUrlParams(String url, String paramName, String params) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }

        StringBuilder sb = new StringBuilder(url);
        if (url.contains("?")) {
            sb.append("&");
        } else {
            sb.append("?");
        }
        sb.append(paramName);
        sb.append("=");
        sb.append(params);
        return sb.toString();
    }

    /***
     *
     * @param string
     * @return 出错时返回-1
     */
    public static int paserStr2Int(String string) {
        try {
            int parseInt = Integer.parseInt(string);
            return parseInt;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

    }

    /**
     * 比较两个字符串（大小写敏感）。
     *
     * <pre>
     *
     *    StringUtil.equals(null, null)   = true
     *    StringUtil.equals(null, &quot;abc&quot;)  = false
     *    StringUtil.equals(&quot;abc&quot;, null)  = false
     *    StringUtil.equals(&quot;abc&quot;, &quot;abc&quot;) = true
     *    StringUtil.equals(&quot;abc&quot;, &quot;ABC&quot;) = false
     *
     * </pre>
     *
     * @param str1 要比较的字符串1
     * @param str2 要比较的字符串2
     * @return 如果两个字符串相同，或者都是 <code>null</code> ，则返回 <code>true</code>
     */
    public static boolean equals(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        }

        return str1.equals(str2);
    }

    /**
     * 检查密码是否符合规范 9-18位，数字、符号、英文
     *
     * @param pwd
     * @return
     */
    public static boolean isPwdRegexp(String pwd) {
        if (TextUtils.isEmpty(pwd)) {
            return false;
        }
        String regexp = "^(?![0-9]+$)(?![a-z]+$)(?![A-Z]+$)(?!([^(0-9a-zA-Z)]|[\\(\\)])+$)([^(0-9a-zA-Z)]|[\\(\\)]|[a-z]|[A-Z]|[0-9]){9,18}$";
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(pwd);
        return m.matches();
    }

    /**
     * @param context
     * @param contentUrl
     */
    public static void copyUrl(Context context, String contentUrl) {
        try {
            ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cmb != null) {
                cmb.setPrimaryClip(ClipData.newPlainText(null, contentUrl));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 验证是否是分享的链接
     *
     * @param url
     * @return
     */
    public static boolean isShareUrl(String url) {
        Pattern p = Pattern.compile("^.*/s/(\\w{15})/download#.*$");
        Matcher m = p.matcher(url);
        return m.matches();
    }

    /**
     * 获取剪切板内容
     *
     * @return
     */
    public static String getClipBoardPaste(Context context) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null && manager.getPrimaryClip() != null) {
            if (manager.hasPrimaryClip() && manager.getPrimaryClip().getItemCount() > 0) {
                if (manager.getPrimaryClip().getItemAt(0) != null){
                    CharSequence addedText = manager.getPrimaryClip().getItemAt(0).getText();
                    String addedTextString = String.valueOf(addedText);
                    if (!TextUtils.isEmpty(addedTextString)) {
                        return addedTextString;
                    }
                }
            }
        }
        return "";
    }

    /**
     * 清空剪切板
     */
    public static void clear(Context context) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            try {
                manager.setPrimaryClip(manager.getPrimaryClip());
                manager.setPrimaryClip(ClipData.newPlainText("", ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String convertUrl(String folderPath, String fileName) {

        if (TextUtils.isEmpty(folderPath) || TextUtils.isEmpty(fileName)) {
            return "";
        }

        return folderPath.endsWith("/") ? folderPath + fileName
                : folderPath + "/" + fileName;

    }

    public static String getMultiDirs(String folderPath, String uid) {
        //不包含uid 则为usb相关操作的新接口
        String dirs = "";
        if(!folderPath.contains(uid)){
            dirs = folderPath;
            if(!dirs.startsWith("/")){
                dirs = "/"+dirs;
            }
            if (dirs.endsWith("/")) {
                dirs = dirs.substring(0, dirs.length() - 1);
            }
            try {
                dirs = URLDecoder.decode(dirs, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return dirs;
        }

        int index = folderPath.indexOf(uid) + uid.length();
        if (folderPath.endsWith("/")) {
            dirs = folderPath.substring(index, folderPath.length() - 1);
        } else {
            dirs = folderPath.substring(index);
        }
        try {
            dirs = URLDecoder.decode(dirs, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return dirs;
    }
    /**
     * 对url进行编码
     *
     * @param url
     * @return
     */
    public static String encoderUrl(String url) {
        return encoderUrl(url, true);
    }

    /**
     * 对url进行编码
     *
     * @param url
     * @return
     */
    public static String encoderUrl(String url, boolean encodePlus) {
        if (TextUtils.isEmpty(url)){
            return url;
        }
        try {
            url = URLEncoder.encode(url, "utf-8");

            if (encodePlus){
                url = url.replace("+", "%20");
            }
            return url;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //Uri.encode(url, "-![.:/,%?&=]");
        return url;
    }
    /**
     * 对url进行解码
     *
     * @param url
     * @return
     */
    public static String decoderUrl(String url) {
        if (TextUtils.isEmpty(url)){
            return url;
        }
        try {
            url = URLDecoder.decode(url, "utf-8");
            return url;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * 1.+ 表示空格（在 URL 中不能使用空格）                   %20
     * 2./ 分隔目录和子目录                                              %2F
     * 3.? 分隔实际的 URL 和参数                                      %3F
     * 4.% 指定特殊字符                                                  %25
     * 5.# 表示书签                                                         %23
     * 6.& URL 中指定的参数间的分隔符                             %26
     * @param url
     * @return
     */
    public static String encoderFileName(String url) {
        if (TextUtils.isEmpty(url)){
            return url;
        }
        try {
            url = URLEncoder.encode(url, "utf-8");
            //%编码后为%25 , replace % 后会变成%2525 , 所以不进行替换
                url = url.replace("+", "%20")
                        .replace("/", "%2F")
                        .replace("?", "%3F")
//                        .replace("%", "%25")
                        .replace("#", "%23")
                        .replace("&", "%26");
            return url;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //Uri.encode(url, "-![.:/,%?&=]");
        return url;
    }

    /**
     * 对url全路径编码
     *
     * @return
     */
    public static String encoderFullUrl(String url, boolean encodePlus) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }

        //先截取url问号后面的参数（包括问号）
        String subFix = "";
        int index = url.lastIndexOf('?');
        if (-1 != index) {
            subFix = url.substring(index, url.length());
            url = url.substring(0, index);
        }

        //找出http://或https://开头的
        int fromIndex = 0;
        if (url.startsWith("http://")) {
            fromIndex = 7;
        } else if (url.startsWith("https://")) {
            fromIndex = 8;
        }

        StringBuilder sb = new StringBuilder();
        if (fromIndex != 0) {
            sb.append(url.substring(0, fromIndex));
            try {
                int pathIndex = url.indexOf("/", fromIndex);
                if (pathIndex != -1) {
                    sb.append(url.substring(fromIndex, pathIndex));
                    url = url.substring(pathIndex);
                }else {
                    //没有path，只有域名，直接return
                    return url + subFix;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!TextUtils.isEmpty(url)) {
            String[] paths = url.split("/");
            for (String path : paths) {
                try {
                    path = URLEncoder.encode(path, "UTF-8");
                    if (encodePlus){
                        path = path.replace("+", "%20").replace("#", "%23");
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                sb.append(path);
                sb.append("/");
            }
            sb.deleteCharAt(sb.length() - 1);
            //添加?后面的
            if (!TextUtils.isEmpty(subFix)){
                sb.append(subFix);
            }
        }
        return sb.toString();
    }

    public static String encoderLocalFullUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }

        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(url)) {
            String[] paths = url.split("/");
            for (String path : paths) {
                try {
                    path = URLEncoder.encode(path, "UTF-8");
                    path = path.replace("+", "%20")
                            .replace("/", "%2F")
                            .replace("?", "%3F")
//                        .replace("%", "%25")
                            .replace("#", "%23")
                            .replace("&", "%26");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                sb.append(path);
                sb.append("/");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * 对url全路径编码,默认对+号处理
     *
     * @return
     */
    public static String encoderFullUrl(String url) {
        return encoderFullUrl(url, true);
    }

    /**
     * 判断 str 是否已经 URLEncoder.encode() 过
     * @param str 需要判断的内容
     * @return 返回 {@code true} 为被 URLEncoder.encode() 过
     */
    public static boolean hasUrlEncoded(String str) {
        boolean encode = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '%' && (i + 2) < str.length()) {
                // 判断是否符合urlEncode规范
                char c1 = str.charAt(i + 1);
                char c2 = str.charAt(i + 2);
                if (isValidHexChar(c1) && isValidHexChar(c2)) {
                    encode = true;
                    break;
                }
            }
        }
        return encode;
    }

    /**
     * 判断 c 是否是 16 进制的字符
     *
     * @param c 需要判断的字符
     * @return 返回 {@code true} 为 16 进制的字符
     */
    private static boolean isValidHexChar(char c) {
        return ('0' <= c && c <= '9') || ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F');
    }



    /**
     * 对url最后的文件名编码
     *
     * @return
     */
    public static String encoderUrlAfterUidPath(String url, String uid) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }
        if (TextUtils.isEmpty(uid)) {
            return url;
        }

        //去除url问号后面的参数（包括问号）
        int index = url.lastIndexOf('?');
        if (-1 != index) {
            url = url.substring(0, index);
        }

        String[] urls = url.split(uid);
        StringBuilder sb = new StringBuilder(urls[0] + uid);
        if (urls.length > 1) {
            String[] paths = urls[1].split("/");
            for (String path : paths) {
                try {
                    path = URLEncoder.encode(path, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                sb.append(path);
                sb.append("/");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * 版本号比较
     *
     * @param v1
     * @param v2
     * @return 0代表相等，1代表左边大，-1代表右边大
     * Utils.compareVersion("1.0.358_20180820090554","1.0.358_20180820090553")=1
     */
    public static int compareVersion(String v1, String v2) {
        if (v1.equals(v2)) {
            return 0;
        }
        String[] version1Array = v1.split("[._]");
        String[] version2Array = v2.split("[._]");
        int index = 0;
        int minLen = Math.min(version1Array.length, version2Array.length);
        long diff = 0;

        while (index < minLen
                && (diff = Long.parseLong(version1Array[index])
                - Long.parseLong(version2Array[index])) == 0) {
            index++;
        }
        if (diff == 0) {
            for (int i = index; i < version1Array.length; i++) {
                if (Long.parseLong(version1Array[i]) > 0) {
                    return 1;
                }
            }

            for (int i = index; i < version2Array.length; i++) {
                if (Long.parseLong(version2Array[i]) > 0) {
                    return -1;
                }
            }
            return 0;
        } else {
            return diff > 0 ? 1 : -1;
        }

    }

    public static String getDeviceMacFromUrl(String url) {
        try {
            if (!url.contains(":") && !url.contains("/") && url.length() < 3) {
                return "";
            }
            int len1 = url.indexOf(":") + 3;
            int len = url.indexOf('/', len1);
            return url.substring(len1, len).split("\\.")[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String disposeUnit(String s) {
        try {
            s = s.toUpperCase();
            String a = Pattern.compile("[^a-zA-Z)]").matcher(s).replaceAll("");
            String num = Pattern.compile("[(a-zA-Z)]").matcher(s).replaceAll("");
            Float parseInt = Float.parseFloat(num);
            DecimalFormat df = new DecimalFormat("0.00");
            if (a.startsWith("E")) {
                return df.format(parseInt * 1024 * 1024 * 1024 * 1024 * 1024 * 1024);
            } else if (a.startsWith("P")) {
                return df.format(parseInt * 1024 * 1024 * 1024 * 1024 * 1024);
            } else if (a.startsWith("T")) {
                return df.format(parseInt * 1024 * 1024 * 1024 * 1024);
            } else if (a.startsWith("G")) {
                return df.format(parseInt * 1024 * 1024 * 1024);
            } else if (a.startsWith("M")) {
                return df.format(parseInt * 1024 * 1024);
            } else if (a.startsWith("KB")) {
                return df.format(parseInt * 1024);
            } else if (a.startsWith("B")) {
                return df.format(parseInt);
            }
        } catch (NumberFormatException e) {
            return "0";
        }
        return "0";
    }

    // 判断一个字符是否是中文
    private static boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;// 根据字节码判断
    }

    // 判断一个字符串是否含有中文  所有不包含中文
    public static boolean isChinese(String str) {
        if (str == null)
            return false;
        for (char c : str.toCharArray()) {
            if (isChinese(c))
                return true;
        }
        return false;
    }

    /**
     * 获取url的ip
     *
     * @param uri
     * @return
     */
    public static String getIP(URI uri) {
        String effectiveURI = "";

        try {
            // URI(String scheme, String userInfo, String host, int port, String
            // path, String query,String fragment)
            effectiveURI = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null).toString();
        } catch (Throwable var4) {
            effectiveURI = "";
        }

        return effectiveURI;
    }


    /**
     * 根据文本得到固定电话号码
     * @param text
     * @return
     */
    public static String getServicePhone(String text) {
        Pattern pattern = Pattern.compile("\\d+(-\\d{1,10})+");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            return "tel:"+matcher.group();
        }
        return "tel:400-998-9866";
    }

    /**
     * 判断filename 是否包含特殊字符  . 和 *
     * 包含则不予处理
     * @param url
     * @return
     */
    public static boolean isIncludeSpecialChar(String url) {
        if (TextUtils.isEmpty(url)){
            return false;
        }
        if(TextUtils.equals(url , "*") || TextUtils.equals(url,".")
                || url.startsWith("/") || url.startsWith("\\")){
            return true;
        }
        return false;
    }

    /**
     * 切割etag . 获取_分割符合后面的MD5 用于匹配本地路径
     * @param oldEtag 旧的etag
     * @return
     */
    public static String splitEtag(String oldEtag){
        if (TextUtils.isEmpty(oldEtag) || !oldEtag.contains("_")){
            return "";
        }
        String etag = oldEtag.substring(oldEtag.lastIndexOf("_")+1);
        //处理webdav返回的etag带双引号
        if(TextUtils.isEmpty(etag)){
            return "";
        }
        if(etag.contains("\"")){
            etag = etag.replace("\"", "");
        }

        return etag;
    }

    /**
     * 根据文件url获取fileId
     * @param url
     * @return
     */
    public static String getFileId(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        Pattern p= Pattern.compile("fileId=(\\w+)&");
        Matcher m = p.matcher(url);
        if (m.find()){
            return m.group(1);
        }
        return "";
    }

}
