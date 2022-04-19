package org.tensorflow.demo;

import java.util.*;

public class WordBreak {
    public String wordBreak(String str, Set<String> dict) {
        if(dict.contains(str))
            return str;

        String ret = new String();
        String pre = new String();
        int i, j, n, flag;
        i = j = n = flag = 0;

        while(j <= str.length()){
            pre = str.substring(i, j);
            //System.out.println(pre);
            if(dict.contains(pre)) {
                flag = 1;
                ret += pre + " ";
                n = j;
            }
            /*else if(!dict.contains(pre)) {
                ret += pre + " ";
                n = j;
            }*/
            if(j == str.length() && i < n) {
                i = j = n;
            }
            j++;
        }
        if(flag != 1)
            ret += pre + " ";
        return ret;
    }

    public static void main(String[] args) {
        HashSet h = new HashSet();

        h.add("for"); h.add("your");
        h.add("will"); h.add("begin");

        h.add("shortly");h.add("por");

        h.add("were");h.add("presentation");

        WordBreak wb = new WordBreak();

        System.out.println(wb.wordBreak("aaaaab", h));
        System.out.println(wb.wordBreak("thethere", h));
        System.out.println(wb.wordBreak("peanutbutter", h));
        System.out.println(wb.wordBreak("peanutfoooo", h));
        System.out.println(wb.wordBreak("peanutbutterfoo", h));
        System.out.println(wb.wordBreak("peanutbuttersalty", h));
    }
}
