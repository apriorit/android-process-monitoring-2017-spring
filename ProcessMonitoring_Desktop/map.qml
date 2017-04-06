import QtQuick 2.0
import QtPositioning 5.5
import QtLocation 5.6

Rectangle {
    width: 720
    height: 480

    property double latitude:  0
    property double longitude: 0
    property double device_latitude: 0
    property double device_longitude: 0

    property variant location: QtPositioning.coordinate(latitude, longitude)
    property variant deviceLocation: QtPositioning.coordinate(device_latitude, device_longitude)

    Plugin {
        id: myPlugin
        name: "osm"
    }

    Map {
        id: map
        anchors.fill: parent
        plugin: myPlugin;
        center: location
        zoomLevel: 13

        MapQuickItem {
                id:marker
                sourceItem: Image{
                    id: image
                    source: "marker.png"

                }
                coordinate: QtPositioning.coordinate(device_latitude, device_longitude)
                anchorPoint.x: image.width / 2
                anchorPoint.y: image.height / 2
            }

            MouseArea {
                anchors.fill: parent
                /*onPressed: {
                    marker.coordinate = map.toCoordinate(QtPositioning.coordinate(device_latitude, device_longitude))
                }*/
            }
    }
}
