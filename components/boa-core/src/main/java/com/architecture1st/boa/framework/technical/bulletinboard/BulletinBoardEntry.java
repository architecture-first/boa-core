package com.architecture1st.boa.framework.technical.bulletinboard;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * An entry for the bulletin board
 */
@AllArgsConstructor
@Data
public class BulletinBoardEntry {
    private String status;
    private String timestamp;
    private String subject;
    private String message;
}
