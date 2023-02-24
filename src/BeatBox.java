import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;

public class BeatBox {
  JPanel mainPanel;
  Sequencer sequencer;
  Sequence seq;
  Track track;
  JFrame frame;
  HashMap<String, JCheckBox[]> instrumentBeats;

  String[] instrumentsLabels = {
      "Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap", "High Tom",
      "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo",
      "Open Hi Conga"
  };

  int[] instrumentKeys = { 35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63 };

  public static void main(String[] args) {
    BeatBox beatBox = new BeatBox();
    beatBox.buildGUI();
  }

  public void buildGUI() {
    frame = new JFrame();
    instrumentBeats = new HashMap<String, JCheckBox[]>(instrumentKeys.length);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    Box controls = createControls();
    Box grid = createGrid();

    frame.getContentPane().add(BorderLayout.CENTER, grid);
    frame.getContentPane().add(BorderLayout.EAST, controls);

    setupMidi();

    frame.setSize(this.frame.getMinimumSize());
    frame.setVisible(true);
  }

  public Box createGrid() {
    Box grid = new Box(BoxLayout.Y_AXIS);

    for (String instrument : instrumentsLabels) {
      Box row = new Box(BoxLayout.X_AXIS);
      JPanel labelPanel = new JPanel();
      labelPanel.setPreferredSize(new Dimension(100, 20));
      labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
      JLabel label = new JLabel(instrument);
      labelPanel.add(label);
      row.add(labelPanel);

      instrumentBeats.put(instrument, new JCheckBox[16]);

      JCheckBox[] beats = instrumentBeats.get(instrument);
      for (int i = 0; i < beats.length; i++) {
        beats[i] = new JCheckBox();
        beats[i].setSelected(false);
        row.add(beats[i]);
      }

      grid.add(row);

      createSeperator(grid);
    }

    return grid;

  }

  public void createSeperator(Box grid) {
    JSeparator separator = new JSeparator();
    separator.setSize(100, 10);
    grid.add(separator);
  }

  public void setupMidi() {
    try {
      sequencer = MidiSystem.getSequencer();
      sequencer.open();
      seq = new Sequence(Sequence.PPQ, 4);
      track = seq.createTrack();
      sequencer.setTempoInBPM(120);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Box createControls() {
    Box controls = new Box(BoxLayout.Y_AXIS);

    JButton startBtn = new JButton("Start");
    startBtn.addActionListener(new startActionListener());
    controls.add(startBtn);

    JButton stopBtn = new JButton("Stop");
    stopBtn.addActionListener(new stopActionListener());
    controls.add(stopBtn);

    JButton increaseTempo = new JButton("Increase Tempo");
    increaseTempo.addActionListener(new increaseTempoListener());
    controls.add(increaseTempo);

    JButton decreaseTempo = new JButton("Decrease Tempo");
    decreaseTempo.addActionListener(new decreaseTempoListener());
    controls.add(decreaseTempo);

    return controls;
  }

  public void buildTrackAndStart(HashMap<String, JCheckBox[]> instrumentBeats) {
    int[] trackList = null;

    seq.deleteTrack(track);
    track = seq.createTrack();

    for (String k : instrumentBeats.keySet()) {
      trackList = new int[16];
      int index = Arrays.asList(instrumentsLabels).indexOf(k);
      int key = instrumentKeys[index];

      for (int i = 0; i < 16; i++) {
        JCheckBox checkBox = instrumentBeats.get(k)[i];
        if (checkBox.isSelected()) {
          trackList[i] = key;
        } else {
          trackList[i] = 0;
        }
      }

      makeTrack(trackList);
      track.add(makeEvent(176, 1, 127, 0, 16));
    }

    track.add(makeEvent(192, 9, 1, 0, 15));

    try {
      sequencer.setSequence(seq);
      sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
      sequencer.start();
      sequencer.setTempoInBPM(60);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void makeTrack(int[] trackList) {
    for (int i : trackList) {
      if (i != 0) {
        track.add(makeEvent(144, 9, i, 100, i));
        track.add(makeEvent(144, 9, i, 100, i + 1));

      }
    }
  }

  public MidiEvent makeEvent(int type, int channel, int note, int velocity, int tick) {
    MidiEvent event = null;

    try {
      ShortMessage a = new ShortMessage();
      a.setMessage(type, channel, note, velocity);
      event = new MidiEvent(a, tick);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return event;
  }

  public class startActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      System.out.println("Start");
      buildTrackAndStart(instrumentBeats);
    }
  }

  public class stopActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      System.out.println("Stop");
      sequencer.stop();
    }
  }

  public class increaseTempoListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      System.out.println("Increase Tempo");
    }
  }

  public class decreaseTempoListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      System.out.println("Decrease Tempo");
    }
  }
}
