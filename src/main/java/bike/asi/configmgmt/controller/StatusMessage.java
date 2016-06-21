package bike.asi.configmgmt.controller;

import lombok.Data;

/**
 *
 * @author gheinze
 */
@Data
public class StatusMessage {

    private final boolean successful;
    private final String msg;

}
