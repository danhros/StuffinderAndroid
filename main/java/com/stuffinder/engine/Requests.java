package com.stuffinder.engine;

import com.stuffinder.data.Profile;
import com.stuffinder.data.Tag;

import java.util.List;

/**
 * Created by propriétaire on 14/03/2015.
 */
public class Requests {

    static abstract class  Request
    {
        private static int nextRequestNumber = 0;

        private final int requestNumber;
        private RequestType requestType;

        protected Request(RequestType requestType)
        {
            requestNumber = nextRequestNumber++;
            this.requestType = requestType;
        }

        public RequestType getRequestType()
        {
            return requestType;
        }

        public int getRequestNumber() {
            return requestNumber;
        }
    }

    static class ModifyEmailRequest extends Request
    {
        private String newEmailAddress;

        ModifyEmailRequest(String newEmailAddress)
        {
            super(RequestType.MODIFY_EMAIL);
            this.newEmailAddress = newEmailAddress;
        }

        String getNewEmailAddress()
        {
            return newEmailAddress;
        }

        public String toString()
        {
            return "Modify email request : new email = " + newEmailAddress;
        }
    }

    static class ModifyPasswordRequest extends Request
    {
        private String newPassword;

        ModifyPasswordRequest(String newPassword)
        {
            super(RequestType.MODIFY_PASSWORD);
            this.newPassword = newPassword;
        }

        String getNewPassword()
        {
            return newPassword;
        }

        public String toString()
        {
            return "Modify password request : new password = " + newPassword;
        }
    }

    static class ModifyBraceletUIDRequest extends Request
    {
        private String newBraceletUID;

        ModifyBraceletUIDRequest(String newBraceletUID)
        {
            super(RequestType.MODIFY_BRACELET_UID);
            this.newBraceletUID = newBraceletUID;
        }

        String getNewBraceletUID()
        {
            return newBraceletUID;
        }

        public String toString()
        {
            return "Modify bracelet UID : new bracelet UID = " + newBraceletUID;
        }
    }

    static class AddTagRequest extends Request
    {
        private Tag tag;
        AddTagRequest(Tag tag)
        {
            super(RequestType.ADD_TAG);
            this.tag = tag;
        }

        Tag getNewTag()
        {
            return tag;
        }

        public String toString()
        {
            return "Add tag request : tag to be modified = (" + tag.getUid() + ", " + tag.getObjectName() + ", "  + tag.getObjectImageName() + ")";
        }
    }

    static class ModifyTagObjectNameRequest extends Request
    {
        private Tag tag;
        private String newObjectName;

        ModifyTagObjectNameRequest(Tag tag, String newObjectName)
        {
            super(RequestType.MODIFY_TAG_OBJECT_NAME);
            this.tag = tag;
            this.newObjectName = newObjectName;
        }

        Tag getTag()
        {
            return tag;
        }

        String getNewObjectName()
        {
            return newObjectName;
        }

        public String toString()
        {
            return "Modify tag object name request : tag to be modified = (" + tag.getUid() + ", " + tag.getObjectName() + ") and new object name = \"" + newObjectName + "\"";
        }
    }

    static class ModifyTagObjectImageRequest extends Request
    {
        private Tag tag;
        private String newObjectImageFilename;

        ModifyTagObjectImageRequest(Tag tag, String newObjectImageFilename)
        {
            super(RequestType.MODIFY_TAG_OBJECT_IMAGE);
            this.tag = tag;
            this.newObjectImageFilename = newObjectImageFilename;
        }

        Tag getTag()
        {
            return tag;
        }

        String getNewObjectImageFilename()
        {
            return newObjectImageFilename;
        }

        public String toString()
        {
            return "Modify tag object image request : tag to be modified = (" + tag.getUid() + ", " + tag.getObjectName() + ") and new image filename = \"" + newObjectImageFilename + "\"";
        }
    }

    static class RemoveTagRequest extends Request
    {
        private Tag tag;

        RemoveTagRequest(Tag tag)
        {
            super(RequestType.REMOVE_TAG);
            this.tag = tag;
        }

        Tag getTag()
        {
            return tag;
        }

        public String toString()
        {
            return "Remove tag request : tag to be removed = (" + tag.getUid() + ", " + tag.getObjectName() + ")";
        }
    }


    static class CreateProfileWithTagsRequest extends Request
    {
        private Profile profile;

        CreateProfileWithTagsRequest(Profile profile) {
            super(RequestType.CREATE_PROFILE_WITH_TAGS);

            this.profile = profile;
        }

        public Profile getProfile() {
            return profile;
        }

        public String toString()
        {
            return "Create profile : new profile = (" + profile.getName() + ", " + profile.getTags().size() + " tags)";
        }
    }

    static class AddTagsToProfileRequest extends Request
    {
        private String profileName;
        private List<Tag> tagsToAdd;

        AddTagsToProfileRequest(String profileName, List<Tag> tagsToAdd) {
            super(RequestType.ADD_TAGS_TO_PROFILE);

            this.profileName = profileName;
            this.tagsToAdd = tagsToAdd;
        }

        public String getProfileName() {
            return profileName;
        }

        public List<Tag> getTagsToAdd() {
            return tagsToAdd;
        }
    }

    static class RemoveTagsFromProfileRequest extends Request
    {
        private String profileName;
        private List<Tag> tagsToRemove;

        RemoveTagsFromProfileRequest(String profileName, List<Tag> tagsToRemove) {
            super(RequestType.REMOVE_TAGS_FROM_PROFILE);

            this.profileName = profileName;
            this.tagsToRemove = tagsToRemove;
        }

        public String getProfileName() {
            return profileName;
        }

        public List<Tag> getTagsToRemove() {
            return tagsToRemove;
        }
    }

    static class ReplaceTagListOfProfileRequest extends Request
    {
        private String profileName;
        private List<Tag> newTagList;

        ReplaceTagListOfProfileRequest(String profileName, List<Tag> newTagList) {
            super(RequestType.REPLACE_TAG_LIST_OF_PROFILE);

            this.profileName = profileName;
            this.newTagList = newTagList;
        }

        public String getProfileName() {
            return profileName;
        }

        public List<Tag> getNewTagList() {
            return newTagList;
        }
    }

    static class ModifyProfileNameRequest extends Request
    {
        private String profileName;
        private String newProfileName;

        ModifyProfileNameRequest(String profileName, String newProfileName) {
            super(RequestType.MODIFY_PROFILE_NAME);

            this.profileName = profileName;
            this.newProfileName = newProfileName;
        }

        public String getProfileName() {
            return profileName;
        }

        public String getNewProfileName() {
            return newProfileName;
        }
    }

    static class RemoveProfileRequest extends Request
    {
        private String profileName;

        RemoveProfileRequest(String profileName) {
            super(RequestType.REMOVE_PROFILE);

            this.profileName = profileName;
        }

        public String getProfileName() {
            return profileName;
        }
    }



}