package com.nnems.jamil;

public class UserData {
  private   String mChapter;
  private   String mVerse;
  private   String mTranslation;
  private   String mReciter;

    public UserData() {

    }

    public UserData(String chapter, String verse, String translation, String reciter) {
        mChapter = chapter;
        mVerse = verse;
        mTranslation = translation;
        mReciter = reciter;
    }

    public String getChapter() {
        return mChapter;
    }

    public void setChapter(String chapter) {
        mChapter = chapter;
    }

    public String getVerse() {
        return mVerse;
    }

    public void setVerse(String verse) {
        mVerse = verse;
    }

    public String getTranslation() {
        return mTranslation;
    }

    public void setTranslation(String translation) {
        mTranslation = translation;
    }

    public String getReciter() {
        return mReciter;
    }

    public void setReciter(String reciter) {
        mReciter = reciter;
    }


    public String getAyat(){
        return mChapter+":"+mVerse;
    }
}
