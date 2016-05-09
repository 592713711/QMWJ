package com.ld.qmwj.util;
import com.ld.qmwj.model.Contacts;
import net.sourceforge.pinyin4j.PinyinHelper;
import java.util.Comparator;

/**
 * 对联系人 Contacts的name 的拼音排序
 * Created by zsg on 2016/5/5.
 */
public class ComparatorPinYin implements Comparator<Contacts> {


    private String ToPinYinString(Contacts contacts){
        String str=contacts.getName();
        StringBuilder sb=new StringBuilder();
        String[] arr=null;

        for(int i=0;i<str.length();i++){
            arr= PinyinHelper.toHanyuPinyinStringArray(str.charAt(i));
            if(arr!=null && arr.length>0){
                for (String string : arr) {
                    sb.append(string);
                }
            }
        }

        return sb.toString();
    }

    @Override
    public int compare(Contacts lhs, Contacts rhs) {
        return ToPinYinString(lhs).compareTo(ToPinYinString(rhs));
    }
}
