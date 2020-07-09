import edu.princeton.cs.algs4.Picture;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SeamCarver {
    private Picture pic;
    private boolean horizontal;
    private boolean fromhorizontal;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException("Null picture!");
        }
        this.pic = new Picture(picture);
        horizontal = false;
        fromhorizontal = false;
    }

    // current picture
    public Picture picture() {
        checkHorizontal();
        return new Picture(pic);
    }

    // width of current picture
    public int width() {
        checkHorizontal();
        return pic.width();
    }

    // height of current picture
    public int height() {
        checkHorizontal();
        return pic.height();
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (x < 0 || y < 0 || x > width() - 1 || y > height() - 1) {
            throw new IllegalArgumentException("Point not in array!");
        }
        checkHorizontal();
        if (x == 0 || y == 0 || x == width() - 1 || y == height() - 1) {
            return 1000;
        } else {
            int lx = pic.getRGB(x - 1, y);
            int hx = pic.getRGB(x + 1, y);
            int ly = pic.getRGB(x, y - 1);
            int hy = pic.getRGB(x, y + 1);
            int total = 0;
            for (int i = 0; i < 3; i++) {
                total += ((lx & 0xFF) - (hx & 0xFF))*((lx & 0xFF) - (hx & 0xFF)) +
                        ((ly & 0xFF) - (hy & 0xFF))*((ly & 0xFF) - (hy & 0xFF));
                lx >>= 8;
                hx >>= 8;
                ly >>= 8;
                hy >>= 8;
            }
            return Math.sqrt(total);
        }
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        if (!horizontal) {
            transpose();
            horizontal = true;
        }
        fromhorizontal = true;
        int[] seam = findVerticalSeam();
        fromhorizontal = false;
        return seam;
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        checkHorizontal();
        if (width() == 1) {
            return new int[height()];
        }

        double[][] energy = new double[width()][height()];
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                energy[i][j] = energy(i, j);
            }
        }
        int[] seam = new int[height()];
        double minDistance = -1;
        int minPoint = -1;
        double[][] distTo = new double[width()][height()]; // distance to each point
        int[][] pathTo = new int[width()][height()]; // last point on path
        for (int i = 0; i < height(); i++) {
            for (int j = 0; j < width(); j++) {
                if (i == 0) {
                    distTo[j][i] = 1000;
                }
                if (i < height() - 1) {
                    for (int m : adj(j, i)) {
                        if (distTo[m][i + 1] == 0 || distTo[m][i + 1] > distTo[j][i] + energy[m][i + 1]) {
                            distTo[m][i + 1] = distTo[j][i] + energy[m][i + 1];
                            pathTo[m][i + 1] = j;
                        }
                    }
                }
                if (i == height() - 1) {
                    if (distTo[j][i] < minDistance || minDistance == -1) {
                        minDistance = distTo[j][i];
                        minPoint = j;
                    }
                }
            }
        }
        seam[seam.length - 1] = minPoint;
        for (int i = seam.length - 2; i >= 0; i--) {
            seam[i] = pathTo[seam[i + 1]][i + 1];
        }
        return seam;
    }

    // returns all pixels that flow from (x, y)
    private Iterable<Integer> adj(int x, int y) {
        int[] adj;
        if (x > 0 && x < width() - 1) {
            adj = new int[3];
            adj[0] = x - 1;
            adj[1] = x;
            adj[2] = x + 1;
        } else if (x == 0) {
            adj = new int[2];
            adj[1] = x + 1;
        } else {
            adj = new int[2];
            adj[0] = x - 1;
            adj[1] = x;
        }
        return () -> new Iterator<>() {
            int i = 0;
            @Override
            public boolean hasNext() {
                if (y == height() - 1) {
                    return false;
                } else {
                    return (i < adj.length);
                }
            }

            @Override
            public Integer next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No more adjacent points!");
                }
                return adj[i++];
            }
        };
    }

    // transposes the picture
    private void transpose() {
        Picture tpic = new Picture(pic.height(), pic.width());
        for (int i = 0; i < pic.width(); i++) {
            for (int j = 0; j < pic.height(); j++) {
                tpic.setRGB(j, i, pic.getRGB(i, j));
            }
        }
        pic = tpic;
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (!horizontal) {
            transpose();
            horizontal = true;
        }
        fromhorizontal = true;
        removeVerticalSeam(seam);
        fromhorizontal = false;
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException("Null seam!");
        }
        if (width() <= 1) {
            throw new IllegalArgumentException("No more seams to remove!");
        }
        checkHorizontal();
        Integer lastSeam = null;
        if (seam.length == height()) {
            Picture newpic = new Picture(width() - 1, height());
            for (int i = 0; i < height(); i++) {
                int index = seam[i];
                if (lastSeam == null) {
                    lastSeam = index;
                }
                if (index < 0 || index > width() - 1) {
                    throw new IllegalArgumentException("Invalid seam");
                }
                if (Math.abs(index - lastSeam) > 1) {
                    throw new IllegalArgumentException("Invalid seam!");
                }
                lastSeam = index;
                for (int j = 0; j < width() - 1; j++) {
                    if (j < index) {
                        newpic.setRGB(j, i, pic.getRGB(j, i));
                    } else {
                        newpic.setRGB(j, i, pic.getRGB(j + 1, i));
                    }
                }
            }
            pic = newpic;
        } else {
            throw new IllegalArgumentException("Invalid seam!");
        }
    }

    private void checkHorizontal() {
        if (horizontal && !fromhorizontal) {
            transpose();
            horizontal = false;
        }
    }

    // command line interface
    public static void main(String[] args) {
        SeamCarver sc = new SeamCarver(new Picture(args[0]));
        int width = Integer.parseInt(args[1]);
        int height = Integer.parseInt(args[2]);
        if (width > sc.width() || height > sc.height() || width < 1 || height < 1) {
            throw new IllegalArgumentException("Height and width must be positive and no greater than original");
        } else {
            int oldWidth = sc.width();
            int oldHeight = sc.height();
            for (int i = 0; i < oldWidth - width; i++) {
                sc.removeVerticalSeam(sc.findVerticalSeam());
            }
            for (int i = 0; i < oldHeight - height; i++) {
                sc.removeHorizontalSeam(sc.findHorizontalSeam());
            }
            sc.pic.save(args[0]);
        }
    }
}