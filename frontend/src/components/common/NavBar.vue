<template>
  <div id="inspire">
    <v-navigation-drawer
      v-model="drawer"
      fixed-tabs
      app
      left
      disable-resize-watcher="true"
      temporary="true"
    >
      <v-list rounded>
        <v-list-item link class="d-flex flex-column pt-8 mb-0">
          <v-avatar color="#eeeeee" size="70" class="user-img mb-1">
            <i v-if="!profileURL" class="fas fa-user"></i>
            <img v-else :src="profileURL" />
          </v-avatar>
          <div class="mt-2">
            <h3 class="text-center">{{ username }}</h3>
            <v-btn
              class="mt-2"
              x-small
              rounded
              color="#999"
              style="opacity:0.7;"
              @click="onLogout"
              dark
              >로그아웃</v-btn
            >
          </div>
        </v-list-item>
        <v-list-item-group color="#0277BD" class="text-center">
          <v-list-item link>
            <v-list-item-content @click="profileMove(user.idx)">
              <v-list-item-title>내 정보</v-list-item-title>
            </v-list-item-content>
          </v-list-item>

          <v-list-item link>
            <v-list-item-content @click="$router.push('/').catch(() => {})">
              <v-list-item-title>팀/팀원 구하기</v-list-item-title>
            </v-list-item-content>
          </v-list-item>

          <v-list-item link>
            <v-expansion-panels :flat="true">
              <v-expansion-panel>
                <v-expansion-panel-header :expand-icon="null">
                  <v-list-item-content>
                    <v-list-item-title style="text-align: center;"
                      >팀 게시판 목록</v-list-item-title
                    >
                  </v-list-item-content>
                </v-expansion-panel-header>
                <v-expansion-panel-content v-if="this.teams.length == 0"
                  >팀 목록이 없습니다.</v-expansion-panel-content
                >
                <v-expansion-panel-content
                  v-else
                  v-for="(team, index) in teams"
                  :key="index"
                >
                  <v-btn text @click="test(team)">{{ team.teamName }}</v-btn>
                </v-expansion-panel-content>
              </v-expansion-panel>
            </v-expansion-panels>
          </v-list-item>

          <v-list-item link>
            <v-list-item-content
              @click="$router.push('/gongmo').catch(() => {})"
            >
              <v-list-item-title>외부 공모전</v-list-item-title>
            </v-list-item-content>
          </v-list-item>
        </v-list-item-group>
      </v-list>
    </v-navigation-drawer>

    <v-app-bar color="#0277BD" app :flat="true" :fixed="true">
      <div class="nav-icon" @click.stop="drawer = !drawer">
        <div></div>
      </div>

      <v-spacer></v-spacer>
      <!-- <img src="../../assets/images/logo_black_title.png" width="60px" alt /> -->
      <p class="title" @click="checkMainURL">neonaedong</p>
      <v-spacer></v-spacer>
      <Search />
      <v-badge :content="messages" :value="messages" color="green" overlap>
        <v-icon
          size="30"
          color="#eeeeee"
          class="ml-3"
          @click="$router.push('/letter').catch(() => {})"
          >mdi-email</v-icon
        >
      </v-badge>
    </v-app-bar>
  </div>
</template>

<script>
import Search from "./Search.vue";
import axios from "axios";
import { EventBus } from "../../main.js";

export default {
  name: "LayoutsDemosBaselineFlipped",
  components: {
    Search,
  },
  props: {
    source: String,
  },
  data: () => ({
    id: 0,
    username: "",
    profileURL: "",
    drawer: null,

    items: [
      { icon: "apps", title: "Home", to: "/" },
      { icon: "bubble_chart", title: "About", to: "/about" },
    ],
    messages: 0,
    letters: [],
    teams: [],
  }),
  created() {
    let token = window.$cookies.get("nnd");
    if (token) {
      this.user = token.object;
      this.username = token.object.name;
      this.profileURL = token.object.profile;
      this.$store.state.myToken = token.object;
    }
    this.getLetters();
    this.getMemberTeamList();

    EventBus.$on("letterRead", () => {
      if (this.messages > 0) {
        this.messages--;
      }
    });
  },
  methods: {
    checkMainURL() {
      if (this.$route.path == "/") {
        this.$router.go().catch(() => {});
      } else {
        this.$router.push("/").catch(() => {});
      }
    },
    profileMove(no) {
      this.$store.state.profileidx = no;
      this.$store.commit("pchange");
      if (this.$route.path == "/Profile") {
        this.$router.go().catch(() => {});
      } else {
        this.$router.push("/Profile").catch(() => {});
      }
    },
    test(no) {
      this.$store.state.teamNo = no.teamboardNo;
      this.$store.state.teamMaster = no.idx;
      this.$store.commit("nchange");
      if (this.$route.path == "/TeamProfile") {
        this.$router.go().catch(() => {});
      } else {
        this.$router.push("/TeamProfile").catch(() => {});
      }
    },
    onLogout: function() {
      this.$store.commit("logout");
      window.$cookies.remove("nnd");
      this.$router.push("/login");
    },
    getLetters() {
      let token = window.$cookies.get("nnd");

      axios
        .get(
          `${process.env.VUE_APP_API_URL}/letter/list/receive/${this.user.idx}`,
          {
            headers: {
              Authorization: "Bearer " + token.data, // the token is a variable which holds the token
            },
          }
        )
        .then((res) => {
          this.letters = res;
          this.messages = this.checkRead(this.letters.data);
        })
        .catch((err) => {
          console.log(err);
        });
    },
    checkRead: function(arr) {
      var count = 0;
      for (let index = 0; index < arr.length; index++) {
        if (arr[index].read == 0) {
          count++;
        }
      }
      return count;
    },
    getMemberTeamList() {
      let token = window.$cookies.get("nnd");

      axios
        .get(
          `${process.env.VUE_APP_API_URL}/teammenu/teamlist/${this.user.idx}`,
          {
            headers: {
              Authorization: "Bearer " + token.data, // the token is a variable which holds the token
            },
          }
        )
        .then((res) => {
          this.teams = res.data;
        })
        .catch((err) => {
          console.log(err);
        });
    },
  },
};
</script>

<style scoped>
.user-img {
  border: 2px solid black;
}
.title {
  margin-bottom: 0;
  font-family: "Rowdies", cursive;
  font-weight: 700;
  font-style: italic;
  color: #eeeeee;
  margin-left: 20px;
}
.point-top {
  height: 90px;
  width: 100%;
  background-color: #3949ab;
  position: absolute;
  opacity: 0.6;
}
.point-bottom {
  height: 40px;
  width: 100%;
  background-color: #283593;
  position: absolute;
  bottom: 0;
  opacity: 0.7;
  font-size: 0.8rem;
  display: flex;
  justify-content: center;
  align-items: center;
}

.point-bottom p {
  margin-bottom: 0;
  color: #fff;
  opacity: 0.8;
}

.nav-icon {
  margin: 0;
  width: 25px;
}

.nav-icon:after,
.nav-icon:before,
.nav-icon div {
  background-color: #eeeeee;
  border-radius: 5px;
  content: "";
  display: block;
  height: 4px;
  margin: 4px 0;
  transition: all 0.2s ease-in-out;
}

.nav-icon:hover:before {
  transform: translateY(8px) rotate(135deg);
}

.nav-icon:hover:after {
  transform: translateY(-8px) rotate(-135deg);
}

.nav-icon:hover div {
  transform: scale(0);
}
</style>
