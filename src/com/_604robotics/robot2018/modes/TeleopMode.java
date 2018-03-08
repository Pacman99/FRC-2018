package com._604robotics.robot2018.modes;

import com._604robotics.robot2018.Robot2018;
import com._604robotics.robot2018.constants.Calibration;
import com._604robotics.robot2018.modules.Arm;
import com._604robotics.robot2018.modules.Clamp;
import com._604robotics.robot2018.modules.Drive;
import com._604robotics.robot2018.modules.Elevator;
import com._604robotics.robot2018.modules.Intake;
import com._604robotics.robotnik.Coordinator;
import com._604robotics.robotnik.Logger;
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
    private final ClampManager clampManager;
    private final Logger test = new Logger("Teleop");

    public TeleopMode (Robot2018 robot) {
        driver.leftStick.x.setDeadband(Calibration.TELEOP_DRIVE_DEADBAND);
        driver.leftStick.y.setDeadband(Calibration.TELEOP_DRIVE_DEADBAND);

        driver.leftStick.x.setFactor(Calibration.TELEOP_FACTOR);
        driver.leftStick.y.setFactor(Calibration.TELEOP_FACTOR);

        driver.rightStick.x.setDeadband(Calibration.TELEOP_DRIVE_DEADBAND);
        driver.rightStick.y.setDeadband(Calibration.TELEOP_DRIVE_DEADBAND);

        //driver.rightStick.x.setFactor(Calibration.TELEOP_FACTOR);
        driver.rightStick.x.setFactor(1); // WEIRD_WHY_?FES:RLJTH *ROHT guirg
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
        clampManager = new ClampManager();
    }
    
    private boolean getHoldElevatorClicks = false;
    private boolean getHoldArmClicks = false;
    
    private double driverLeftJoystickY = 0.0;
    private double driverLeftJoystickX = 0.0;
    private double driverLeftTrigger = 0.0;
    
    private boolean driverLeftJoystickButton = false;
    private boolean driverLeftTriggerButton = false;
    private boolean driverLeftBumper = false;
    
    private double driverRightJoystickY = 0.0;
    private double driverRightJoystickX = 0.0;
    private double driverRightTrigger = 0.0;
    
    private boolean driverRightJoystickButton = false;
    private boolean driverRightTriggerButton = false;
    private boolean driverRightBumper = false;
    
    private boolean driverBack = false;
    private boolean driverStart = false;
    private boolean driverA = false;
    private boolean driverB = false;
    private boolean driverX = false;
    private boolean driverY = false;
    
    private double manipLeftJoystickY = 0.0;
    private double manipLeftJoystickX = 0.0;
    private double manipLeftTrigger = 0.0;
    
    private boolean manipLeftJoystickButton = false;
    private boolean manipLeftTriggerButton = false;
    private boolean manipLeftBumper = false;
    
    private double manipRightJoystickY = 0.0;
    private double manipRightJoystickX = 0.0;
    private double manipRightTrigger = 0.0;
    
    private boolean manipRightJoystickButton= false;
    private boolean manipRightTriggerButton= false;
    private boolean manipRightBumper= false;
    
    private boolean manipBack= false;
    private boolean manipStart= false;
    private boolean manipA= false;
    private boolean manipB= false;
    private boolean manipX= false;
    private boolean manipY= false;
    
    @Override
    public boolean run () {
    	updateControls();
        process();
        return true;
    }
   
    private void updateControls() {
    	driverLeftJoystickY = driver.leftStick.y.get();
        driverLeftJoystickX = driver.leftStick.x.get();
        driverLeftTrigger = driver.triggers.left.get();
        
        driverLeftJoystickButton = driver.buttons.leftStick.get();
        driverLeftTriggerButton = driver.buttons.lt.get();
        driverLeftBumper = driver.buttons.lb.get();
        
        driverRightJoystickY = driver.rightStick.y.get();
        driverRightJoystickX = driver.rightStick.x.get();
        driverRightTrigger = driver.triggers.right.get();
        
        driverRightJoystickButton = driver.buttons.rightStick.get();
        driverRightTriggerButton = driver.buttons.rt.get();
        driverRightBumper = driver.buttons.rb.get();
        
        driverBack = driver.buttons.back.get();
        driverStart = driver.buttons.start.get();
        driverA = driver.buttons.a.get();
        driverB = driver.buttons.b.get();
        driverX = driver.buttons.x.get();
        driverY = driver.buttons.y.get();
        
        manipLeftJoystickY = manip.leftStick.y.get();
        manipLeftJoystickX = manip.leftStick.x.get();
        manipLeftTrigger = manip.triggers.left.get();
        
        manipLeftJoystickButton = manip.buttons.leftStick.get();
        manipLeftTriggerButton = manip.buttons.lt.get();
        manipLeftBumper = manip.buttons.lb.get();
        
        manipRightJoystickY = manip.rightStick.y.get();
        manipRightJoystickX = manip.rightStick.x.get();
        manipRightTrigger = manip.triggers.right.get();
        
        manipRightJoystickButton = manip.buttons.rightStick.get();
        manipRightTriggerButton = manip.buttons.rt.get();
        manipRightBumper = manip.buttons.rb.get();
        
        manipBack = manip.buttons.back.get();
        manipStart = manip.buttons.start.get();
        manipA = manip.buttons.a.get();
        manipB = manip.buttons.b.get();
        manipX = manip.buttons.x.get();
        manipY = manip.buttons.y.get();
        /*System.out.println("driverLeftY: "+driverLeftJoystickY+", driverRightX"+driverRightJoystickX
                +", driverRightY"+driverRightJoystickY);*/
    }
    
    private void process() {
    	driveManager.run();
        elevatorManager.run();
        intakeManager.run();
        armManager.run();
        clampManager.run();
    }
    
    private class ClampManager {
    	private final Clamp.Extend extend;
    	private final Clamp.Retract retract;
    	private final Toggle clamping;
    	
    	public ClampManager() {
    		extend = robot.clamp.new Extend();
    		retract = robot.clamp.new Retract();
    		clamping = new Toggle(false);
    	}
    	
    	public void run() {
    		clamping.update(driverX || manipX);
            if (clamping.isInOnState()) {
                extend.activate();
            } else if (clamping.isInOffState()) {
                retract.activate();
            }
    	}
    }
    
    private class ArmManager {
    	private final Arm.Move move;
    	private final Arm.Setpoint setpoint;
    	private double holdSetpoint = Calibration.ARM_LOW_TARGET;
    	
    	public ArmManager() {
    		move = robot.arm.new Move();
    		setpoint = robot.arm.new Setpoint(Calibration.ARM_LOW_TARGET);
    	}
    	
    	public void run() {
    		if( manipRightJoystickY != 0 ) {
    			move.liftPower.set(manipRightJoystickY/2);
    			move.activate();
    			getHoldArmClicks = true;
    		} else if( manipA ) {
    			setpoint.target_clicks.set(Calibration.ARM_LOW_TARGET);
    			setpoint.activate();
    			getHoldArmClicks = true;
    		} else if( manipB ) {
    		    if (manipLeftBumper) {
    		        setpoint.target_clicks.set(Calibration.ARM_HIGH_TARGET);
    		    } else {
    		        setpoint.target_clicks.set(Calibration.ARM_MID_TARGET);
    		    }
    			setpoint.activate();
    			getHoldArmClicks = true;
    		} else if( manipY ) {
    			setpoint.target_clicks.set(Calibration.ARM_HIGH_TARGET);
    			setpoint.activate();
    			getHoldArmClicks = true;
    		} else if( driverA ) {
    			setpoint.target_clicks.set(Calibration.ARM_LOW_TARGET);
    			setpoint.activate();
    			getHoldArmClicks = true;
    		} else if( driverB ) {
                if (driverLeftJoystickButton) {
                    setpoint.target_clicks.set(Calibration.ARM_HIGH_TARGET);
                } else {
                    setpoint.target_clicks.set(Calibration.ARM_MID_TARGET);
                }
    			setpoint.activate();
    			getHoldArmClicks = true;
    		} else if( driverY ) {
    			setpoint.target_clicks.set(Calibration.ARM_HIGH_TARGET);
    			setpoint.activate();
    			getHoldArmClicks = true;
    		} else {
    		    // This should only be called once
                if( getHoldArmClicks ) {
                    test.log("WARN","Activate arm hold with setpoint "+robot.arm.encoderClicks.get());
                    holdSetpoint=robot.arm.encoderClicks.get();
                    robot.arm.resetIntegral(Calibration.ARM_RESET_SUM);
                    getHoldArmClicks = false;
                }
                setpoint.target_clicks.set(holdSetpoint);
                setpoint.activate();
    		}
    	}
    }
    
    
    private class IntakeManager {
    	private final Intake.Idle idle;
    	private final Intake.Run run;
    	
    	public IntakeManager() {
    		idle = robot.intake.new Idle();
    		run = robot.intake.new Run();
    	}
    	
    	public void run() {
    		if( driverLeftTrigger != 0 || driverRightTrigger != 0 ) {
    			double output = 0;
    			if( driverLeftTrigger != 0 ) {
    				output = -0.7*(driverLeftTrigger*driverLeftTrigger);
    			} else if( driverRightTrigger != 0 ) {
    				output = 0.7*driverRightTrigger;
    			}
    			run.runPower.set(output);
    			run.activate();
    		} else if( manipLeftTrigger != 0 || manipRightTrigger != 0 ) {
    			double output = 0;
    		    if( manipLeftTrigger != 0 ) {
    		    	output = -0.7*(manipLeftTrigger*manipLeftTrigger);
    		    } else if( manipRightTrigger != 0 ) {
    		    	output = 0.7*manipRightTrigger;
    		    }
    		    run.runPower.set(output);
    		    run.activate();
    		} else {
    			idle.activate();
    		}
    	}
    }
    
    private class ElevatorManager {
        private final Elevator.Move move;
        private final Elevator.Setpoint setpoint;
        private double holdSetpoint;

        public ElevatorManager() {
            move = robot.elevator.new Move();
            setpoint = robot.elevator.new Setpoint();
        }

        public void run() {
            // Only use when absolutely necessary
        	/*if( driverStart || manipStart ) {
        		 robot.elevator.encoder.zero();
                 setpoint.target_clicks.set(robot.elevator.encoderClicks.get());
                 setpoint.activate();
        	} else */if( manipLeftJoystickY != 0 && !manipLeftBumper ) {
        	    // Scale negative power for safety
        	    double elevPower = manipLeftJoystickY;
        	    if (elevPower<0) {
        	        elevPower*=0.5;
        	    }
        		move.liftPower.set(elevPower);
        		move.activate();
        		getHoldElevatorClicks = true;
        	} else if( manipA ) {
        		setpoint.target_clicks.set(Calibration.ELEVATOR_LOW_TARGET);
        		setpoint.activate();
        		getHoldElevatorClicks = true;
        	} else if( manipB && manipLeftBumper ) {
        		setpoint.target_clicks.set(Calibration.ELEVATOR_MID_TARGET);
        		setpoint.activate();
        		getHoldElevatorClicks = true;
        	} else if( manipY && manipLeftBumper ) {
        		setpoint.target_clicks.set(Calibration.ELEVATOR_HIGH_TARGET);
        		setpoint.activate();
        		getHoldElevatorClicks = true;
        	} else if( driverA ) {
        		setpoint.target_clicks.set(Calibration.ELEVATOR_LOW_TARGET);
        		setpoint.activate();
        		getHoldElevatorClicks = true;
        	} else if( driverB && driverLeftJoystickButton ) {
        		setpoint.target_clicks.set(Calibration.ELEVATOR_MID_TARGET);
        		setpoint.activate();
        		getHoldElevatorClicks = true;
        	} else if( driverY && driverLeftJoystickButton ) {
        		setpoint.target_clicks.set(Calibration.ELEVATOR_HIGH_TARGET);
        		setpoint.activate();
        		getHoldElevatorClicks = true;
        	} else {
        	    // This should only be called once
        		if( getHoldElevatorClicks ) {
        		    test.log("WARN","Activate elevator hold with setpoint "+robot.elevator.encoderClicks.get());
        			holdSetpoint=robot.elevator.encoderClicks.get();
        			robot.elevator.resetIntegral(Calibration.ELEVATOR_RESET_SUM);
        			getHoldElevatorClicks = false;
        		}
        		// No need to hold up at such a low level
        		// Reduce quiescent current consumption in these cases
        		if (holdSetpoint<Calibration.ELEVATOR_BUMPER_CLEAR) {
        			move.liftPower.set(0.0);
        			move.activate();
        		} else {
        			setpoint.target_clicks.set(holdSetpoint);
        			setpoint.activate();
        		}
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
        	double leftY = driver.leftStick.y.get();
        	double rightY = driver.rightStick.y.get();
        	double rightX = driver.rightStick.x.get();
            // Set gears
            gearState.update(driverRightBumper);
            // Will probably be double solenoid but waiting
            if (gearState.isInOnState()) {
                robot.shifter.highGear.activate();
            } else if (gearState.isInOffState()) {
                robot.shifter.lowGear.activate();
            }
            // Flip values if xbox inverted
            inverted.update(driverLeftBumper);
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
