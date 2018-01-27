package com._604robotics.robot2018.modules;

import com._604robotics.robot2018.constants.Calibration;
import com._604robotics.robot2018.constants.Ports;
import com._604robotics.robotnik.Action;
import com._604robotics.robotnik.Input;
import com._604robotics.robotnik.Module;
import com._604robotics.robotnik.Output;
import com._604robotics.robotnik.prefabs.controller.ClampedIntegralPIDController;
import com._604robotics.robotnik.prefabs.devices.HoldMotor;
import com._604robotics.robotnik.prefabs.flow.SmartTimer;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Spark;

public class Elevator extends Module {
	
	public Spark motor = new Spark(Ports.ELEVATOR_MOTOR);
	public Encoder encoder = new Encoder(Ports.ELEVATOR_ENCODER_A, Ports.ELEVATOR_ENCODER_B);
	public HoldMotor holdMotor = new HoldMotor(motor, encoder, 
	        Calibration.ELEVATOR_TARGET_SPEED, Calibration.ELEVATOR_CLICK_TOLERANCE);
	
	public final Hold hold = new Hold();
	public final Setpoint setpoint = new Setpoint();
	
	public final Output<Double> getOffset = addOutput("Elevator Offset", this::getOffset);
	public final Output<Double> getUpwardsRange = addOutput("Upwards Range", this::getUpwardsRange);
	public final Output<Double> getDownwardsRange = addOutput("Downwards Range", this::getDownwardsRange);
	public final Output<Boolean> getFailsafe = addOutput("Failsafed", this::getFailsafe);
	
	public final Output<Double> encoderRate = addOutput("Elevator Rate", encoder::getRate);
	public final Output<Integer> encoderClicks = addOutput("Elevator Clicks", encoder::get);
	
	public boolean holding = true;
	public double power = 0;
	
	public final Output<Boolean> getHolding = addOutput("Holding", this::getHolding);
	public final Output<Double> getPower = addOutput("Power", this::getPower);
	
	public final Output<Boolean> atSpeed = addOutput("Elevator At Speed", this::atSpeed);
	public final Output<Boolean> atPosition = addOutput("Elevator At Position", this::atSpeed);
	
	private final ClampedIntegralPIDController pid;
	private final SmartTimer PIDTimer = new SmartTimer();
	
	public boolean atSpeed() {
		return holdMotor.at_speed;
	}
	
	public boolean getHolding() {
		return holding;
	}
	
	public double getPower() {
		return power;
	}
	
	public double getOffset() {
		return holdMotor.offset;
	}
	
	public double getUpwardsRange() {
		return holdMotor.upwardsRange;
	}
	
	public double getDownwardsRange() {
		return holdMotor.downwardsRange;
	}
	
	public boolean getFailsafe() {
		return holdMotor.failsafed;
	}
	
	public class Hold extends Action {
        public Hold () {
            super(Elevator.this, Hold.class);
        }

        @Override
        public void begin () {
        	holding=true;
        }
        @Override
        public void run () {
            holdMotor.hold();
            if (pid.getError()>setpoint.target_clicks.get()) {
            	setpoint.activate();
            }
        }
    }
	
	public class Move extends Action {
		public final Input<Double> liftPower;
		
		public Move() {
			this(0);
		}
		
		public Move (double power) {
			super(Elevator.this, Move.class);
			liftPower = addInput("Lift Power", power, true);
		}
		
		@Override
		public void run () {
			holding = false;
			power = liftPower.get();
			holdMotor.set(liftPower.get());
		}
	}
	
	public class Setpoint extends Action {
		public final Input<Integer> target_clicks;
		private int i=0;
		
		public Setpoint() {
			this(0);
		}
		
		public Setpoint( int clicks) {
			super(Elevator.this, Setpoint.class);
			target_clicks = addInput("Target Clicks", clicks, true);
		}
		
		@Override
		public void begin() {
			System.out.println("Enabling PID");
		    pid.enable();
			holding = false;
		}
		@Override
		public void run () {
			i++;
			pid.setSetpoint(target_clicks.get());
			if (i%100==99) {
				System.out.println("PID Error is "+pid.getError());
			}
			if (Math.abs(pid.getError())<Calibration.ELEVATOR_CLICK_TOLERANCE) {
				PIDTimer.startIfNotRunning();
				if (i%100==0) {
					System.out.println("Timer is now at "+PIDTimer.get());
				}
				if (PIDTimer.get()>Calibration.ELEVATOR_PID_CONTINUE) {
					pid.disable();
					System.out.println("Switching to hold");
					hold.activate();
					PIDTimer.stop();
				}
			} else {
				PIDTimer.restart();
			}
			
			//holdMotor.setpointHold(target_clicks.get());
		}
		@Override
		public void end () {
			System.out.println("Resetting PID");
			PIDTimer.reset();
		    pid.reset();
		}
	}
	
	public Elevator() {
	    super(Elevator.class);
	    encoder.setReverseDirection(true);
	    pid = new ClampedIntegralPIDController(Calibration.ELEVATOR_P,
	            Calibration.ELEVATOR_I,
	            Calibration.ELEVATOR_D,
	            encoder,
	            holdMotor);
	    pid.setIntegralLimits(Calibration.ELEVATOR_MIN_SUM, Calibration.ELEVATOR_MAX_SUM);
	    pid.setOutputRange(Calibration.ELEVATOR_MIN_SPEED, Calibration.ELEVATOR_MAX_SPEED);
        setDefaultAction(hold);
	}
}
