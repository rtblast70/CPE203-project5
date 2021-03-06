import processing.core.PImage;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Charizard extends AnimationEntity implements Moveable{

    public Charizard(String id, Point position,
                   List<PImage> images, int resourceLimit, int resourceCount,
                   int actionPeriod, int animationPeriod)
    {
        super(id, position, images, actionPeriod, animationPeriod, 0);
    }

    public void execute(
            WorldModel world,
            ImageStore imageStore,
            EventScheduler scheduler)
    {
        Optional<Entity> minerTarget =
                world.findNearest(this.position, MinerNotFull.class);
        long nextPeriod = this.actionPeriod;

        if (minerTarget.isPresent()) {
            Point tgtPos = minerTarget.get().getPosition();

            if (this.moveTo(world, minerTarget.get(), scheduler)) {
                Fire fire = CreateFactory.createFire(tgtPos,
                        imageStore.getImageList("fire"));

                world.addEntity(fire);
                nextPeriod += this.actionPeriod;
                fire.scheduleActions(scheduler, world, imageStore);
            }
        }

        scheduler.scheduleEvent(this,
                CreateAction.createActivityAction(this, world, imageStore),
                nextPeriod);
    }

    public boolean moveTo(
            WorldModel world,
            Entity target,
            EventScheduler scheduler)
    {
        if (getPosition().adjacent(target.getPosition())) {
            world.removeEntity(target);
            scheduler.unscheduleAllEvents(target);
            return true;
        }
        else {
            Point nextPos = nextPosition(world, target.getPosition());

            if (!getPosition().equals(nextPos)) {
                Optional<Entity> occupant = world.getOccupant(nextPos);
                if (occupant.isPresent()) {
                    scheduler.unscheduleAllEvents(occupant.get());
                }

                world.moveEntity(this, nextPos);
            }
            return false;
        }
    }

    public Point nextPosition(
            WorldModel world, Point destPos)
    {
        AStarPathingStrategy pathing = new AStarPathingStrategy();

        List<Point> pathPoints = pathing.computePath(this.getPosition(), destPos,
                (p -> world.withinBounds(p) && !(world.isOccupied(p))),
                ((p1, p2) -> p1.adjacent(p2)),
                PathingStrategy.CARDINAL_NEIGHBORS);

        if (pathPoints.isEmpty()){
            return this.getPosition();
        }

        return pathPoints.get(0);
    }


}