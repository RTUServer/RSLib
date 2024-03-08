package com.github.ipecter.rtuserver.lib.util.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MongoInfo {

    private final String ip;
    private final String port;
    private final String username;
    private final String password;
    private final String database;

}
