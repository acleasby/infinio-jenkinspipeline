package com.infinio

public class Version implements Comparable<Version> {
    boolean snapshot
    List<Integer> places = new ArrayList<>()

    public Version(String version) {
        snapshot = version.toLowerCase(Locale.ENGLISH).contains("snapshot")
        def baseVersion = version.replace("-SNAPSHOT", "")
        places.addAll(Arrays.asList(baseVersion.split("\\.")).collect { Integer.parseInt(it) })
    }

    Version(List<Integer> places, boolean snapshot) {
        this.places.addAll(places)
        this.snapshot = snapshot
    }

    public Version increment() {
        int placeToIncrement = 0;
        for (int i = places.size() - 1; i >= 0; i--) {
            if (places[i] != 0) {
                placeToIncrement = i;
                break;
            }
        }
        List<Integer> newPlaces = new ArrayList<>(places);
        newPlaces.set(placeToIncrement, newPlaces.get(placeToIncrement) + 1)
        return new Version(newPlaces, snapshot)
    }

    @Override
    public int compareTo(Version version) {
        if (snapshot != version.snapshot) {
            throw new IllegalArgumentException("Cannot compare snapshot and non-snapshot versions")
        }
        if (version == null) {
            return -1
        }
        int placesToCompare = Math.min(places.size(), version.places.size())
        for (int i = 0; i < placesToCompare; i++) {
            if (places.get(i) < version.places.get(i)) {
                return -1;
            } else if (places.get(i) > version.places.get(i)) {
                return 1;
            }
        }
        if (places.size() < version.places.size()) {
            return -1
        } else if (places.size() > version.places.size()) {
            return 1
        }

        return 0
    }

    @Override
    public String toString() {
        return String.join(".", places.collect { Integer.toString(it) }) +
                (snapshot ? "-SNAPSHOT" : "")
    }

    public boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Version version = (Version) o

        if (places != version.places) return false

        return true
    }

    public int hashCode() {
        return (places != null ? places.hashCode() : 0)
    }
}
