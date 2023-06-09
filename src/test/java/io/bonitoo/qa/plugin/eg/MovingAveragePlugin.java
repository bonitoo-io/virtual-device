package io.bonitoo.qa.plugin.eg;

import io.bonitoo.qa.plugin.PluginProperties;
import io.bonitoo.qa.plugin.item.ItemGenPlugin;
import io.bonitoo.qa.plugin.item.ItemPluginConfigClass;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

@ItemPluginConfigClass(conf = MovingAveragePluginConf.class)
public class MovingAveragePlugin extends ItemGenPlugin {

  static int MAX_QUEUE_LENGTH = 100;

  ArrayBlockingQueue<Double> dataStream = new ArrayBlockingQueue<>(MAX_QUEUE_LENGTH);

  Double max = 55.0;
  Double min = -6.0;

  int window = 10;
  public MovingAveragePlugin(PluginProperties props, boolean enabled){
    super(props, enabled);
  }

  public MovingAveragePlugin() {
    super();
  }

  @Override
  public void applyProps(PluginProperties props) {
     // holder
  }

  @Override
  public void onLoad() {
    this.window = ((MovingAveragePluginConf)item.getConfig()).getWindow();
    populateQueue();
    genData();
  }

  @Override
  public Object genData() {
    calcWindowAverage();
    return item.getVal();
  }

  @Override
  public Object getCurrentVal() {
    return item.getVal();
  }

  public MovingAveragePlugin addDataToQueue(){
    dataStream.add((Math.random() * (this.max - this.min)) + this.min);
    return this;
  }

  public MovingAveragePlugin populateQueue(){

    for(int i = 0; i < MAX_QUEUE_LENGTH; i++){
      addDataToQueue();
    }
    return this;
  }

  public MovingAveragePlugin calcWindowAverage(){

    Double sum = 0.0;

    for(int i = 0; i < window; i++){
      sum += dataStream.poll();
      addDataToQueue();
    }

    item.setVal(sum / window);

    return this;
  }

  public String dumpQueue(){
    return Arrays.toString(dataStream.toArray());
  }
}
