# SEUCityflow

**JAVA**:

1.配置 java 环境

2.git push XXXX

3.导入 IDEA 后运行 src/test/java/Test.java

**PYTHON**:

1.pip install jpype1

2.安装 java（版本应大于 1.8.0_201） 并配置 JAVA_HOME 环境变量

3.下载 SEUCityflow-x.x.x.jar 与 lib.zip，解压 zip 并置于所用文件夹下

4.python 调用方法
``` python
import jpype
from jpype.types import *
import json
if not jpype.isJVMStarted():
    jpype.startJVM(jvmargs, classpath=['/your/path/to/lib/*', '/your/path/to/SEUCityflow-x.x.x.jar'], convertStrings=True) #jvmargs 根据实际数据设定
Engine = jpype.JClass('engine')
engine = Engine("/your/path/to/configFile.json", threadNumber)
```

5.configFile
```
{
  "interval": 1, // 每 step 的时间间隔
  "seed": 47,  // 随机数种子
  "dir": "src/test/resources/manhattan/", // 文件夹地址
  "roadnetFile": "manhattan.json", // roadnet 文件名
  "flowFile": "manhattan_31217.json", // flow 文件名
  "rlTrafficLight": false, // 是否开启强化学习接口
  "laneChange": true, // 是否支持变道
  "saveReplay": true, // 是否保存 
  "routeType": "DYNAMIC", // 含 LENGHT, DURATION, DYNAMIC, RANDOM，用于决定 vehicle 寻找最短路的方式  
  "roadnetLogFile": "replay_manhattan.json", // 保存的 roadnet 文件名，可配合 fronted/index.html 可视化 
  "replayLogFile": "replay_manhattan_31217_LaneChange.txt" // 保存的 log 文件名, 可配合 fronted/index.html 可视化 
}
```

6.API

**注：**
对所有返回值类型为 list 或 dict 的方法，需使用 json.loads() 进行类型转化，如：
```
vehicleNameList = json.loads(engine.get_vehicles(include_waiting=False))
```

**Simulation**

To simulate one step, simply call engine.next_step()
```
engine.next_step()
```

**Data Access API**

```
get_vehicle_count()
```
- Get number of total running vehicles.
- Return an **int**
```
get_finished_vehicle_count()
```
- Get number of total finished vehicles.
- Return an **int**
```
get_vehicles(include_waiting=False)
```
- Get all vehicle ids
- Include vehicles in lane’s waiting buffer
- if include_waiting=True Return an **list** of vehicle ids
 ```
get_lane_vehicle_count()
```
- Get number of running vehicles on each lane.
- Return a **dict** with lane id as key and corresponding number as value.
```
get_lane_waiting_vehicle_count()
```
- Get number of waiting vehicles on each lane. Currently, vehicles with speed less than 0.1m/s is considered as waiting.
- Return a **dict** with lane id as key and corresponding number as value.
```
get_lane_vehicles()
```
- Get vehicle ids on each lane.
- Return a **dict** with lane id as key and list of vehicle id as value.
```
get_vehicle_info(vehicle_id)
```
- Return a **dict** which contains information of the given vehicle.
- The items include:
  - running: whether the vehicle is running.
  - If the vehicle is running:
    - speed: The speed of the vehicle.
    - distance: The distance the vehicle has travelled on the current lane or lanelink.
    - drivable: The id of the current drivable(lane or lanelink)
    - road: The id of the current road if the vehicle is running on a lane.
    - intersection: The next intersection if the vehicle is running on a lane.
    - route: A string contains ids of following roads in the vehicle’s route which are separated by ' '.
- Note that all items are stored as str.
```
get_vehicle_speed()
```
- Get speed of each vehicle
- Return a **dict** with vehicle id as key and corresponding speed as value.
```
get_vehicle_distance()
```
- Get distance travelled on current lane of each vehicle.
- Return a **dict** with vehicle id as key and corresponding distance as value.
```
get_leader(vehicle_id)
```
- Return the id of the vehicle in front of vehicle_id.
- Return an empty **string** "" when vehicle_id does not have a leader
```
get_current_time()
```
- Get simulation time (in seconds)
- Return a **double**
```
get_average_travel_time()
```
- Get average travel time (in seconds)
- Return a **double**

**Control API**
```
set_tl_phase(intersection_id, phase_id)
```
- Set the phase of traffic light of intersection_id to phase_id. Only works when rlTrafficLight is set to true.
- The intersection_id should be defined in roadnetFile
- phase_id is the index of phase in array "lightphases", defined in roadnetFile.
```
set_vehicle_speed(vehicle_id, speed)
```
- Set the speed of vehicle_id to speed.
- The vehicles have to obey fundamental rules to avoid collisions so the real speed might be different from speed.
```
reset(seed=False)
```
- Reset the simulation (clear all vehicles and set simulation time back to zero)
- Reset random seed if seed is set to True
- This does not clear old replays, instead, it appends new replays to replayLogFile.
```
snapshot()
```
- Take a snapshot of current simulation state
- This will generate an Archive object which can be loaded later
- You can save an Archive object to a file using its dump method.
```
load(archive)
```
- Load an Archive object and restore simulation state
```
load_from_file(path)
```
- Load a snapshot file created by dump method and restore simulation state.
```
save_to_file(path)
```
- save now simulation state to file

The whole process of saving and loading file is like:
```
archive = eng.snapshot() # create an archive object
archive.dump("save.json") # if you want to save the snapshot to a file
# or just use eng.save_to_file("save.json")

# do something

eng.load(archive)
# load 'archive' and the simulation will start from the status when 'archive'is created

# or if you want to load from 'save.json'
eng.load_from_file("save.json")
```
```
set_random_seed(seed)
```
- Set seed of random generator to seed
```
set_vehicle_route(vehicle_id, route)
```
- To change the route of a vehicle during its travelling.
- route is a list of road ids (doesn’t include the current road)
  - Return true if the route is available and can be connected.

Other API
```
set_replay_file(replay_file)
```
- replay_file should be a path related to dir in config file
- Set replayLogFile to replay_file, newly generated replays will be output into replay_file
- This is useful when you want to look at a specific episode for debugging purposes
- This API works only when saveReplay is true in config json
```
set_save_replay(open)
```
- Open or close replay saving
- Set open to False to stop replay saving
- Set open to True to start replay saving
- This API works only when saveReplay is true in config json
