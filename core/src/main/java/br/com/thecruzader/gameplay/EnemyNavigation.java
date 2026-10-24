package br.com.thecruzader.gameplay;

import br.com.thecruzader.level.Bounds;
import br.com.thecruzader.level.LevelData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Walkable surfaces and collision-checked jumps for ordinary, equally sized enemies. */
final class EnemyNavigation {
    private static final double WIDTH = LevelData.EnemyKind.SENTINEL.width;
    private static final double HEIGHT = LevelData.EnemyKind.SENTINEL.height;
    private static final double SPEED = 150, JUMP_SPEED = 220, JUMP = 570, GRAVITY = 1450;
    private static final double ARC_JUMP = JUMP - GRAVITY * GameWorld.STEP / 2;
    private record Surface(double left, double right, double y) {}
    private record Link(int from, int to, double launch, double vx, boolean jump) {}
    private final List<Surface> surfaces = new ArrayList<>();
    private final List<List<Link>> outgoing = new ArrayList<>(), incoming = new ArrayList<>();
    private final int[] hops, queue;

    EnemyNavigation(LevelData level) {
        for (Bounds floor : level.solids()) {
            List<Surface> portions = new ArrayList<>(List.of(new Surface(floor.x(), floor.right() - WIDTH, floor.top())));
            for (Bounds obstacle : level.solids()) {
                if (obstacle.y() >= floor.top() + HEIGHT || obstacle.top() <= floor.top()) continue;
                var cut = new ArrayList<Surface>();
                for (Surface portion : portions) {
                    double left = obstacle.x() - WIDTH, right = obstacle.right();
                    if (right <= portion.left || left >= portion.right) cut.add(portion);
                    else {
                        if (left > portion.left) cut.add(new Surface(portion.left, left, portion.y));
                        if (right < portion.right) cut.add(new Surface(right, portion.right, portion.y));
                    }
                }
                portions = cut;
            }
            for (Surface portion : portions) if (portion.right >= portion.left) surfaces.add(portion);
        }
        for (int i = 0; i < surfaces.size(); i++) {
            outgoing.add(new ArrayList<>());
            incoming.add(new ArrayList<>());
        }
        for (int from = 0; from < surfaces.size(); from++)
            for (int to = 0; to < surfaces.size(); to++) if (from != to) connect(from, to, level.solids());
        hops = new int[surfaces.size()];
        queue = new int[surfaces.size()];
    }

    private void connect(int from, int to, List<Bounds> solids) {
        Surface a = surfaces.get(from), b = surfaces.get(to);
        double gap = Math.max(a.left, b.left) - Math.min(a.right, b.right);
        if (Math.abs(b.y - a.y) <= 32 && gap >= 0 && gap <= WIDTH + .01) {
            add(new Link(from, to, a.right <= b.left ? a.right : a.left, 0, false));
        }
        double discriminant = ARC_JUMP * ARC_JUMP - 2 * GRAVITY * (b.y - a.y);
        if (discriminant < 0) return;
        double flight = (ARC_JUMP + Math.sqrt(discriminant)) / GRAVITY;
        double margin = Math.min(4, (b.right - b.left) / 2);
        double takeoffMargin = Math.min(2, (a.right - a.left) / 2);
        for (int direction : new int[] {-1, 1}) {
            double[] landings = {b.left + margin, b.right - margin, (b.left + b.right) / 2,
                    clamp((a.left + a.right) / 2 + direction * JUMP_SPEED * flight, b.left + margin, b.right - margin),
                    clamp(a.left + 4 + direction * JUMP_SPEED * flight, b.left + margin, b.right - margin),
                    clamp(a.right - 4 + direction * JUMP_SPEED * flight, b.left + margin, b.right - margin)};
            for (double landing : landings) {
                double launch = landing - direction * JUMP_SPEED * flight;
                if (launch < a.left + takeoffMargin || launch > a.right - takeoffMargin) continue;
                if (clearArc(launch, a.y, direction * JUMP_SPEED, flight, solids))
                    add(new Link(from, to, launch, direction * JUMP_SPEED, true));
            }
        }
    }

    private boolean clearArc(double launch, double y, double vx, double flight, List<Bounds> solids) {
        for (double t = GameWorld.STEP; t < flight; t += GameWorld.STEP) {
            double x = launch + vx * t, bottom = y + ARC_JUMP * t - GRAVITY * t * t / 2;
            for (Bounds solid : solids) if (solid.overlaps(x, bottom, WIDTH, HEIGHT)) return false;
        }
        return true;
    }

    private void add(Link link) {
        outgoing.get(link.from).add(link);
        incoming.get(link.to).add(link);
    }

    void pursue(Enemy enemy, double targetX, double targetY, double dt) {
        if (!enemy.grounded) {
            enemy.vx = enemy.jumpVx != 0 ? enemy.jumpVx : Math.signum(targetX - enemy.x) * SPEED;
            return;
        }
        enemy.jumpVx = 0;
        int from = surfaceAt(enemy.x, enemy.y), goal = surfaceAt(targetX, targetY);
        if (from < 0 || goal < 0) return;
        if (from == goal) {
            walk(enemy, clamp(targetX, surfaces.get(goal).left, surfaces.get(goal).right), dt);
            return;
        }
        Arrays.fill(hops, -1);
        hops[goal] = 0;
        int head = 0, tail = 0;
        queue[tail++] = goal;
        while (head < tail) {
            int node = queue[head++];
            for (Link link : incoming.get(node)) if (hops[link.from] < 0) {
                hops[link.from] = hops[node] + 1;
                queue[tail++] = link.from;
            }
        }
        Link next = null;
        for (Link link : outgoing.get(from)) {
            if (hops[link.to] < 0 || hops[link.to] >= hops[from]) continue;
            if (next == null || hops[link.to] < hops[next.to]
                    || (hops[link.to] == hops[next.to] && Math.abs(link.launch - enemy.x) < Math.abs(next.launch - enemy.x)))
                next = link;
        }
        if (next == null) return;
        if (!next.jump) {
            Surface destination = surfaces.get(next.to);
            walk(enemy, (destination.left + destination.right) / 2, dt);
        } else if (Math.abs(enemy.x - next.launch) > 1) {
            walk(enemy, next.launch, dt);
        } else {
            enemy.vy = JUMP;
            enemy.grounded = false;
            enemy.vx = enemy.jumpVx = next.vx;
            enemy.facing = next.vx < 0 ? -1 : 1;
        }
    }

    void patrol(Enemy enemy, double dt) {
        if (!enemy.grounded) return;
        enemy.jumpVx = 0;
        int node = surfaceAt(enemy.x, enemy.y);
        if (node < 0) return;
        Surface floor = surfaces.get(node);
        double left = Math.max(floor.left, enemy.patrolCenter - 70);
        double right = Math.min(floor.right, enemy.patrolCenter + 70);
        double next = enemy.x + enemy.facing * 22 * dt;
        if (next < left || next > right || (enemy.state == Enemy.State.PATROL && enemy.blockedTime > .15))
            enemy.facing = -enemy.facing;
        enemy.vx = enemy.facing * 22;
    }

    private int surfaceAt(double x, double y) {
        int result = -1;
        double highest = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < surfaces.size(); i++) {
            Surface surface = surfaces.get(i);
            if (x >= surface.left - WIDTH / 2 && x <= surface.right + WIDTH / 2 && surface.y <= y + 1 && surface.y > highest) {
                result = i;
                highest = surface.y;
            }
        }
        return result;
    }

    private static void walk(Enemy enemy, double target, double dt) {
        double distance = target - enemy.x;
        enemy.vx = Math.signum(distance) * Math.min(Math.abs(distance) / dt, SPEED);
        if (enemy.vx != 0) enemy.facing = enemy.vx < 0 ? -1 : 1;
    }
    private static double clamp(double value, double low, double high) { return Math.max(low, Math.min(high, value)); }
}
