package dev.vavateam1.dao;

import dev.vavateam1.model.Location;
import java.util.List;

public interface LocationDao {
    List<Location> findAll();

    Location createLocation(String name);

    void updateLocationName(int locationId, String name);

    void softDeleteLocation(int locationId);
}
