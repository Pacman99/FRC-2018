package com._604robotics.robot2018.modes;

import com._604robotics.robot2018.Robot2018;
import com._604robotics.robot2018.constants.Calibration;
import com._604robotics.robot2018.modules.Arm;
import com._604robotics.robot2018.modules.Drive;
import com._604robotics.robot2018.modules.Elevator;
import com._604robotics.robot2018.modules.Intake;
import com._604robotics.robotnik.Coordinator;
import com._604robotics.robotnik.prefabs.flow.Pulse;
import com._604robotics.robotnik.prefabs.flow.Toggle;
import com._604robotics.robotnik.prefabs.inputcontroller.xbox.XboxController;

public class TeleopMode extends Coordinator {

    private final XboxController driver = new XboxController(0);
    private final XboxController manip = new XboxController(1);

    private final Robot2018 robot;

    private final DriveManager driveManager;
    private final ElevatorManager elevatorManager;
    private final IntakeManager intakeManager;
    private final ArmManager armManager;


    public TeleopMode (Robot2018 robot) {
        driver.leftStick.x.setDeadband(Calibration.TELEOP_DRIVE_DEADBAND);
        driver.leftStick.y.setDeadband(Calibration.TELEOP_DRIVE_DEADBAND);

        driver.leftStick.x.setFactor(Calibration.TELEOP_FACTOR);
        driver.leftStick.y.setFactor(Calibration.TELEOP_FACTOR);

        driver.rightStick.x.setDeadband(Calibration.TELEOP_DRIVE_DEADBAND);
        driver.rightStick.y.setDeadband(Calibration.TELEOP_DRIVE_DEADBAND);

        driver.rightStick.x.setFactor(Calibration.TELEOP_FACTOR);
        driver.rightStick.y.setFactor(Calibration.TELEOP_FACTOR);

        manip.leftStick.x.setDeadband(Calibration.TELEOP_MANIP_DEADBAND);
        manip.leftStick.y.setDeadband(Calibration.TELEOP_MANIP_DEADBAND);

        manip.leftStick.x.setFactor(Calibration.TELEOP_FACTOR);
        manip.leftStick.y.setFactor(Calibration.TELEOP_FACTOR);

        manip.rightStick.x.setDeadband(Calibration.TELEOP_MANIP_DEADBAND);
        manip.rightStick.y.setDeadband(Calibration.TELEOP_MANIP_DEADBAND);

        manip.rightStick.x.setFactor(Calibration.TELEOP_FACTOR);
        manip.rightStick.y.setFactor(Calibration.TELEOP_FACTOR);

        this.robot = robot;

        driveManager = new DriveManager();
        elevatorManager = new ElevatorManager();
        intakeManager = new IntakeManager();
        armManager = new ArmManager();
    }

    @Override
    public boolean run () {
        driveManager.run();
        elevatorManager.run();
        intakeManager.run();
        armManager.run();
        return true;
    }
   
    private class ArmManager {
    	private final Arm.Move move;
    	private final Arm.Setpoint setpoint;
    	
    	public ArmManager() {
    		move = robot.arm.new Move();
    		setpoint = robot.arm.new Setpoint();
    	}
    	
    	public void run() {
    		// TODO; literally everything
    	}
    }
    
    
    private class IntakeManager {
    	private final Intake.Idle idle;
    	private final Intake.Spit spit;
    	private final Intake.Suck suck;
    	
    	public IntakeManager() {
    		idle = robot.intake.new Idle();
    		spit = robot.intake.new Spit();
    		suck = robot.intake.new Suck();
    	}
    	
    	public void run() {
    		double leftTrigger = driver.triggers.left.get();
    		double rightTrigger = driver.triggers.right.get();
    		if( leftTrigger == 0 && rightTrigger == 0 ) {
    			idle.activate();
    		} else if( leftTrigger != 0 ) {
    			suck.suckPower.set(leftTrigger*leftTrigger);
    		} else if( rightTrigger != 0 ) {
    			spit.spitPower.set(rightTrigger*rightTrigger);
    		}
    	}
    }
    
    private class ElevatorManager {
        private final Elevator.Move move;
        private final Elevator.Setpoint setpoint;
        private boolean isStationary=false;
        private Pulse manualMove = new Pulse();
        private double holdClicks = 0;

        public ElevatorManager() {
            move = robot.elevator.new Move();
            setpoint = robot.elevator.new Setpoint();
            manualMove.update(false);
        }

        public void run() {
            //System.out.println(robot.elevator.getEncoderPos());
            double leftY = manip.leftStick.y.get();
            boolean start = manip.buttons.start.get();
            System.out.println("Error is "+robot.elevator.pidError.get());
            if (start) {
                robot.elevator.encoder.zero();
                holdClicks = robot.elevator.encoderClicks.get();
                setpoint.target_clicks.set(holdClicks);
                setpoint.activate();
                // TODO: Restructure into else later
                return;
            }
            if( manip.buttons.y.get() ) {
                isStationary=false;
                setpoint.target_clicks.set(Calibration.ELEVATOR_Y_TARGET);
                setpoint.activate();
                manualMove.update(false);
            } else if ( manip.buttons.x.get() ) {
                isStationary=false;
                setpoint.target_clicks.set(Calibration.ELEVATOR_X_TARGET);
                setpoint.activate();
                manualMove.update(false);
            } else if ( manip.buttons.b.get() ) {
                isStationary=false;
                setpoint.target_clicks.set(Calibration.ELEVATOR_B_TARGET);
                setpoint.activate();
                manualMove.update(false);
            } else if ( manip.buttons.a.get() ) {
                isStationary=false;
                setpoint.target_clicks.set(Calibration.ELEVATOR_A_TARGET);
                setpoint.activate();
                manualMove.update(false);
            } else {
                if( leftY == 0 ) {
                    if (!isStationary) {
                        isStationary=true;
                        holdClicks=robot.elevator.encoderClicks.get();
                    }
                    setpoint.target_clicks.set(holdClicks);
                    setpoint.activate();
                    manualMove.update(false);
                } else {
                    isStationary=false;
                    move.liftPower.set(leftY);
                    move.activate();
                    manualMove.update(true);
                }
            }
            if (manualMove.isFallingEdge()) {
                robot.elevator.resetIntegral(Calibration.ELEVATOR_INTEGRAL_RESET);
            }
        }
    }
    
    private enum CurrentDrive {
        IDLE, ARCADE, TANK
    }

    private class DriveManager {
        private final Drive.ArcadeDrive arcade;
        private final Drive.TankDrive tank;
        private final Drive.Idle idle;
        private CurrentDrive currentDrive;
        private Toggle inverted;
        private Toggle gearState;

        public DriveManager () {
            idle=robot.drive.new Idle();
            arcade=robot.drive.new ArcadeDrive();
            tank=robot.drive.new TankDrive();
            // TODO: Expose on dashboard
            currentDrive=CurrentDrive.ARCADE;
            // TODO: Expose on dashboard
            inverted=new Toggle(false);
            gearState=new Toggle(false);
        }

        public void run() {
            // Set gears
            gearState.update(driver.buttons.lb.get());
            // Will probably be double solenoid but waiting
            /*if (gearState.isInOnState()) {
                robot.shifter.highGear.activate();
            } else if (gearState.isInOffState()) {
                robot.shifter.lowGear.activate();
            }*/
            // Get Xbox data
            double leftY=driver.leftStick.y.get();
            double rightX=driver.rightStick.x.get();
            double rightY=driver.rightStick.y.get();
            // Flip values if xbox inverted
            inverted.update(driver.buttons.rb.get());
            robot.dashboard.XboxFlipped.set(inverted.isInOnState());
            if (inverted.isInOnState()) {
                leftY*=-1;
                rightY*=-1;
            }
            // Get Dashboard option for drive
            switch (robot.dashboard.driveMode.get()){
                case OFF:
                    currentDrive=CurrentDrive.IDLE;
                    break;
                case ARCADE:
                    currentDrive=CurrentDrive.ARCADE;
                    break;
                case TANK:
                    currentDrive=CurrentDrive.TANK;
                    break;
                case DYNAMIC:
                    // Dynamic Drive mode detection logic
                    if (currentDrive == CurrentDrive.TANK) {
                        if (Math.abs(rightY) <= 0.2 && Math.abs(rightX) > 0.3) {
                            currentDrive = CurrentDrive.ARCADE;
                        }
                    } else { // currentDrive == CurrentDrive.ARCADE
                        if (Math.abs(rightX) <= 0.2 && Math.abs(rightY) > 0.3) {
                            currentDrive = CurrentDrive.TANK;
                        }
                    }
                    break;
                default:
                    System.out.println("This should never happen!");
                    System.out.println("Current value is:"+robot.dashboard.driveMode.get());
            }

            // Set appropriate drive mode depending on dashboard option
            switch (currentDrive) {
                case IDLE:
                    idle.activate();
                    break;
                case ARCADE:
                    arcade.movePower.set(leftY);
                    arcade.rotatePower.set(rightX);
                    arcade.activate();
                    break;
                case TANK:
                    tank.leftPower.set(leftY);
                    tank.rightPower.set(rightY);
                    tank.activate();
                    break;
            }
        }
    }
}
