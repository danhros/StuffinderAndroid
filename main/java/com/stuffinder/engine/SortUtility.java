package com.stuffinder.engine;

import com.stuffinder.data.Profile;
import com.stuffinder.data.Tag;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by propriétaire on 04/04/2015.
 */
public class SortUtility {
    public static void sortTagListByUID(List <Tag> tagList)
    {
        Collections.sort(tagList, new Comparator<Tag>() {
            @Override
            public int compare(Tag lhs, Tag rhs) {
                return lhs.getUid().compareTo(rhs.getUid());
            }
        });
    }

    public static void sortTagListByObjectName(List <Tag> tagList)
    {
        Collections.sort(tagList, new Comparator<Tag>() {
            @Override
            public int compare(Tag lhs, Tag rhs) {
                return lhs.getObjectName().compareToIgnoreCase(rhs.getObjectName());
            }
        });
    }

    public static void sortProfileList(List<Profile> profileList)
    {
        Collections.sort(profileList, new Comparator<Profile>() {
            @Override
            public int compare(Profile lhs, Profile rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });
    }

    public static Tag getTagByUID(List<Tag> tagList, String UID)
    {
        return getTagByUID1(tagList, UID, 0, tagList.size() - 1);
    }

    private static Tag getTagByUID1(List<Tag> tagList, String UID, int begin, int end)
    {
        if(begin > end)
            return null;

        int middle = (end - begin)/2 + begin;

        Tag middleTag = tagList.get(middle);
        int res = UID.compareTo(middleTag.getUid());

        if(res == 0)
            return middleTag;
        else if(res > 0) // means middleTag.getUID() < UID
            return getTagByUID1(tagList, UID, middle + 1, end);
        else
            return getTagByUID1(tagList, UID, begin, middle - 1);

    }
}
