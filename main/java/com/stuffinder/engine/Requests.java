package com.stuffinder.engine;

import com.stuffinder.data.Tag;

/**
 * Created by propriétaire on 14/03/2015.
 */
public class Requests {

    static abstract class  Request
    {
        private RequestType requestType;

        protected Request(RequestType requestType)
        {
            this.requestType = requestType;
        }

        RequestType getRequestType()
        {
            return requestType;
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

}