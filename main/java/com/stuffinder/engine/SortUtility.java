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
}
