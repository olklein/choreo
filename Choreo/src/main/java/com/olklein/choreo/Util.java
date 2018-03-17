package com.olklein.choreo;

/**
 * Created by olklein on 12/03/2018.
 */


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;



public class Util {

    static String[] slowAndQuickString = {"Q","S"};
    private static final String[] beatsString = {"1","2","3","4","5","6","7","8"};

    private static Map<Character, Integer> getSlowAndQuickFreq(String s) {
        Map<Character, Integer> charFreq = new HashMap<Character, Integer>();
        String newChar;

        if(s!=null){
            for (Character c:s.toCharArray()){
                newChar = c.toString().toUpperCase();
                if (newChar.equals(slowAndQuickString[0]) || newChar.equals(slowAndQuickString[1])) {
                    Integer count = charFreq.get(newChar.charAt(0));
                    int newCount = (count == null ? 1 : count + 1);
                    charFreq.put(newChar.charAt(0), newCount);
                }
            }
        }
        return charFreq;
    }
    private static final Character one    = '1';
    private static final Character eight = '8';

    private static Map<Character, Integer> getBeatsFreq(String s) {
        Map<Character, Integer> charFreq = new HashMap<Character, Integer>();
        Character newChar;

        if(s!=null){
            for (Character c:s.toCharArray()){
                newChar = c.toString().toLowerCase().charAt(0);
                if (newChar.charValue()>=one.charValue() && newChar.charValue()<=eight.charValue()) {
                    Integer count = charFreq.get(newChar);
                    int newCount = (count == null ? 1 : count + 1);
                    charFreq.put(newChar, newCount);
                }
            }
        }
        return charFreq;
    }


    public static int[] ChoreoLength(String rhythmString){

        Map<Character, Integer> slowAndQuickCounts  = Util.getSlowAndQuickFreq(rhythmString);
        Map<Character, Integer> beatsCounts         = Util.getBeatsFreq(rhythmString);
        Integer count;
        int slowCount = 0;
        String charString = slowAndQuickString[0];
        count = slowAndQuickCounts.get(charString.charAt(0));
        if (count !=null) slowCount=count;

        int quickCount= 0;
        charString = slowAndQuickString[1];;
        count=slowAndQuickCounts.get(charString.charAt(0));
        if (count !=null) quickCount=count;

        int beatsCount = 0;
        if (!beatsCounts.isEmpty()) {
            for (String s : beatsString){
                 count =beatsCounts.get(s.charAt(0));
                if (count != null) beatsCount += count;
            }
        }
        int totalBeatsCount = slowCount*2+quickCount+beatsCount;
        int[] result = {slowCount, quickCount, beatsCount, totalBeatsCount};
        return result;
    }

    public static String getNewName(String newFilename, File directory) {
        int addCounter = 1;

        int lastDotIndex =newFilename.lastIndexOf('.');
        String NameWithoutExtension;
        String extension;

        if (lastDotIndex>=0) {
            NameWithoutExtension = newFilename.substring(0, lastDotIndex);
            extension = newFilename.substring(lastDotIndex);
        }else{
            NameWithoutExtension = newFilename;
            extension ="";
        }

        String updatedFilename = newFilename;
        Boolean foundIndex=false;

        ArrayList<String> fileList = new ArrayList<>();

        if (null != directory) {
            if (directory.exists() && directory.isDirectory()) {
                File[] listOfFiles= directory.listFiles();

                for (File file : listOfFiles) {
                    String filename = file.getName();
                    fileList.add(filename);
                    foundIndex =(filename.toLowerCase().equals(newFilename.toLowerCase())?true:foundIndex);
                }
            }
        }
        if (!foundIndex) {
            return newFilename;
        }else{
            if (fileList.size() > 0) {
                Collections.sort(fileList, new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                        return s1.compareTo(s2);
                    }
                });
                boolean reDoIt;

                do {
                    foundIndex = false;
                    for (String filename : fileList) {
                        foundIndex =(filename.toLowerCase().equals(updatedFilename.toLowerCase())?true:foundIndex);
                    }

                    if (!foundIndex) {
                        reDoIt = false;
                    }else{
                        updatedFilename = NameWithoutExtension + " (" + addCounter + ")"+extension;
                        addCounter++;
                        reDoIt = true;
                    }
                } while (reDoIt);
                return updatedFilename;
            } else {
                return newFilename;
            }
        }
    }
}


