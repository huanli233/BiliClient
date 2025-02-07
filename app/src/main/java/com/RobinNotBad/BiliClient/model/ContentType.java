package com.RobinNotBad.BiliClient.model;

public enum ContentType {
    Video(1),
    Topic(2),
    Activity(4),
    ShortVideo(5),
    BlackRoom(6),
    Announcement(7),
    Live(8),
    ActivityContent(9),
    LiveAnnouncement(10),
    Photo(11),
    Article(12),
    Ticket(13),
    Audio(14),
    Judgement(15),
    Review(16),
    Dynamic(17),
    VideoPlaylist(18),
    AudioPlaylist(19),
    Manga_1(20),
    Manga_2(21),
    Manga_3(22),
    Course(33),
    ;

    private final int typeCode;

    ContentType(int typeCode) {
        this.typeCode = typeCode;
    }

    public int getTypeCode() {
        return typeCode;
    }

    public static ContentType getContentType(int typeCode) throws TerminalIllegalTypeCodeException {
        for (ContentType contentType : ContentType.values()) {
            if (contentType.getTypeCode() == typeCode) {
                return contentType;
            }
        }
        throw new TerminalIllegalTypeCodeException();
    }

    public static class TerminalIllegalTypeCodeException extends Exception {

    }
}
