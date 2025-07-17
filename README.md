# 贪吃蛇(升级版)

## 武汉科技大学 重生为蛇，无限进化小组 倾情制作

#### [SaiQI Jin](https:#github.com/jinsaiqi)，[Bleader Yang](https:#github.com/Bleaderr) ，[Ian Tao](https:#github.com/Seal367)

IDEA 打开直玩！

## 📢 News

- **2025-07-17**:贪吃蛇第二次更新，修改了界面显示问题、操作提示问题、同时美化界面。

- **2025-07-16**:贪吃蛇第一次更新，修改了暂停的bug、技能无法更换、buff重叠的问题。

- **2025-07-15**: 贪吃蛇（升级版）首次开源 🚀。
- **2025-07-14**:贪吃蛇Demo开始制作.。



## 🦾 Introductions

图形界面与文字描述双重版本！满足您对原版贪吃蛇的想法！

![alt](https://github.com/Seal367/wust_java_online/blob/main/imgs/1.png?raw=true)

新增rougue模式！十五种技能任你选择！穿墙、加速、无敌随意选择！

![alt](https://github.com/Seal367/wust_java_online/blob/main/imgs/2.png?raw=true)

炫酷UI超强技能，任意游玩！

![alt](https://github.com/Seal367/wust_java_online/blob/main/imgs/4.png?raw=true)

![alt](https://github.com/Seal367/wust_java_online/blob/main/imgs/3.png?raw=true)

## 📊 File Structure

以下是我们项目主要文件树构成，符合java开发包管理规则。

```text
src/
└── main/
    ├── java/
    │   ├── module-info.java
    │   └── com/
    │       └── example/
    │           └── javafx3/
    │               ├── SnakeGameWSAD.java  # Game start flie
    │               ├── GameOverTest.java # Game-over test file
    │               ├── SkillSystemTest.java # Skill test file
    │               ├── SimpleSnakeGame.java # Platform-game start file
    │               ├── EffectType.java # EffectEnum
    │               ├── Rarity.java # RarityEnum
    │               ├── ui/
    │               │   ├── SkillMenuController.java # Controller for menu
    │               │   ├── SkillDetailController.java # Controller for skill
    │               │   ├── GameOverController.java # Controller for game-over
    │               │   ├── SkillNotificationSystem.java # Controller for skill notification
    │               │   ├── SkillCardController.java # Controller for card
    │               │   └── SimpleSkillMenuController.java # Controller for skill mune
    │               ├── model/
    │               │   ├── SkillCard.java # Class of skillCard
    │               │   └── SkillEffect.java # Class of skillEffect
    │               └── manager/
    │                   ├── SkillManager.java # Manage skill
    │                   └── GameState.java # Manage gamestate
    └── resources/
        ├── css/
        │   ├── skill-detail.css # Draw skill detail
        │   ├── game-over.css # Draw game-over page
        │   ├── skill-ui.css # draw skill detail
        │   └── game-styles.css # draw game page detail
        └── fxml/
            ├── skill-detail.fxml # Construction of skill detail
            ├── game-over.fxml # Construction of game-over page
            ├── skill-card.fxml # Construction of skill card
            └── skill-menu.fxml # Construction of skill menu
```
## 💡 Quick Start

操作说明：

1. 空格键开始游玩。
2. W、A、S、D控制上下左右。
3. 按P键暂停，调整技能。

剩下内容请玩家自行探索，祝您游玩愉快！

## 😊 Acknowledgement

​	衷心感谢**袁郡**老师这两周来对我们的谆谆教诲，javafx的学习无疑为我们从事互联网相关开发工作添砖加瓦！这对我们的未来真的很有意义！

## 📚 Citation

​	如果你发现我们的开发对你很有帮助，请考虑引用：

```
@wust{tanchishe,
	title={贪吃蛇（升级版）},
	author={Saiqi Jin and Tong Yang and Xiwen Tao},
	year={2025},
	company={Wuhan University of Science and Technology}
}
```

