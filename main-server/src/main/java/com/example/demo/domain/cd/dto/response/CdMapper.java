package com.example.demo.domain.cd.dto.response;

import com.example.demo.domain.cd.dto.request.CdItemRequest;
import com.example.demo.domain.cd.entity.Cd;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.prop.entity.Prop;

public class CdMapper {

    public static Cd toEntity(Playlist playlist, Prop prop, CdItemRequest req) {
        return Cd.builder()
                .playlist(playlist)
                .prop(prop)
                .xCoordinate(req.xCoordinate())
                .yCoordinate(req.yCoordinate())
                .height(req.height())
                .width(req.width())
                .scale(req.scale())
                .angle(req.angle())
                .build();
    }
}
