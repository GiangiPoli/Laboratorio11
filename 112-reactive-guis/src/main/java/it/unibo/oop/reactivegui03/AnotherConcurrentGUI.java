package it.unibo.oop.reactivegui03;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.oop.JFrameUtil;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;

/**
 * Second example of reactive GUI.
 */
public final class AnotherConcurrentGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(AnotherConcurrentGUI.class);
    private final JLabel display = new JLabel();

    /**
     * Creating new ConcurrentGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        JFrameUtil.dimensionJFrame(this);
        final JPanel panel = new JPanel();
        panel.add(display);

        final JButton up = new JButton("UP");
        final JButton down = new JButton("DOWN");
        final JButton stop = new JButton("STOP");
        panel.add(up);
        panel.add(down);
        panel.add(stop);

        this.getContentPane().add(panel);
        this.setVisible(true);

        final Agent agent = new Agent();
        new Thread(agent).start();

        final SecondAgent agent2 = new SecondAgent(agent);
        new Thread(agent2).start();

        //Controllo i vari pulsanti
        up.addActionListener(i -> agent.setEnabled());
        down.addActionListener(i -> agent.setEnabled());
        stop.addActionListener(i -> agent.stopCounting());

        if (agent.stop) {
            up.setEnabled(false);
            down.setEnabled(false);
            stop.setEnabled(false);
        }
    }

    private final class Agent implements Runnable {

        private volatile boolean stop;
        private volatile boolean up = true;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    if (up) {
                        counter++;
                    } else {
                        counter--;
                    }
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }

        /**
         * External command to change value directions.
         */
        public void setEnabled() {
            this.up = !this.up;
        }
    }

    private final class SecondAgent implements Runnable {

        private static final long DEFAULT_STOP = 10_000L;
        private final Agent agenteEsterno;

        SecondAgent(final Agent agent) {
            this.agenteEsterno = agent;
        }

        @Override
        public void run() {
            final long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < DEFAULT_STOP) {
                System.out.println("..."); //NOPMD
            }
            this.agenteEsterno.stopCounting();
        }
    } 

}
