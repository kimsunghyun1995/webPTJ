<template>
  <div class="letter">
    <v-card outlined>
      <v-list-item @click="onLetterDetail(`${letterinfo.letterNo}`)" @click.stop="dialog = true">
        <div class="ml-2 mr-3">
          <v-avatar
            color="grey"
            size="50"
            @click="profileMove(item.tab == '받은 편지함' ? letterinfo.sendIdx : letterinfo.receiveIdx)"
          >
            <i v-if="!letterinfo.profile" class="fas fa-user"></i>
            <img v-else :src="letterinfo.profile" />
          </v-avatar>
        </div>
        <v-list-item-content style="position:relative;">
          <div>
            <v-list-item-title v-text="letterinfo.name" class="font-weight-black mb-1"></v-list-item-title>
            <!-- 편지 읽음/안읽음  -->
            <div class="d-flex">
              <v-list-item-title
                v-text="letterinfo.content"
                class="text--secondary d-inline-block text-truncate"
                style="max-width: 160px;"
              ></v-list-item-title>
            </div>
          </div>
          <small
            class="mb-0 text--secondary"
            style="position:absolute; right:0; top:15px"
          >{{ letterDate }}</small>
          <div v-if="item.tab == '받은 편지함'" style="position:absolute; right:7px; top:35px">
            <i
              class="fas fa-envelope"
              style="font-size:20px; color:#7986CB"
              v-if="!letterinfo.read"
            ></i>
            <i class="fas fa-envelope-open-text" style="font-size:20px; color: #BDBDBD;" v-else></i>
          </div>
        </v-list-item-content>
      </v-list-item>
      <v-dialog v-model="dialog" min-width="350" max-width="500">
        <LetterDetail
          :item="item"
          :letterinfo="letterinfo"
          :dialog="dialog"
          @changeDialog="dialog = false"
        />
      </v-dialog>
      <div class="type-team" v-if="letterinfo.letterType == 'tboard'"></div>
      <div class="type-member" v-else></div>
    </v-card>
  </div>
</template>

<script>
import LetterDetail from "./LetterDetail.vue";
import axios from "axios";
import { EventBus } from "../../main.js";

export default {
  components: {
    LetterDetail
  },
  data() {
    return {
      dialog: false,
      letterDate: "" // 포맷팅한 날짜를 저장하는 변수공간
    };
  },
  props: {
    item: {
      type: Object
    },
    letterinfo: {
      type: Object
    }
  },
  created() {
    this.letterDate = this.dateFormatted(this.letterinfo.createDate);
  },
  methods: {
    // 클릭한 프로필 페이지로 이동하는 함수
    profileMove(no) {
      this.$store.state.profileidx = no;
      this.$store.commit("pchange");
      if (this.$route.path == "/userProfile") {
        this.$router.go().catch(() => {});
      } else {
        this.$router.push("/userProfile").catch(() => {});
      }
    },
    // 클릭한 편지의 편지 pk를 받아옴.
    onLetterDetail(letterNo) {
      let token = window.$cookies.get("nnd");
      // 클릭한 편지는 읽음 처리
      if (this.item.tab == "받은 편지함") {
        if (this.letterinfo.read == 0) {
          this.letterinfo.read = 1;
          axios
            .post(
              `${process.env.VUE_APP_API_URL}/letter/update/${letterNo}`,
              {},
              {
                headers: {
                  Authorization: "Bearer " + token.data // the token is a variable which holds the token
                }
              }
            )
            .then(() => {
              if (this.item.tab == "받은 편지함") {
                EventBus.$emit("letterRead");
              }
            })
            .catch(err => console.log(err));
        }
      }
    },
    dateFormatted: function(dt) {
      var min = 60 * 1000;
      var c = new Date();
      var d = new Date(dt);
      var minsAgo = Math.floor((c - d) / min);
      if (minsAgo < 0) {
        minsAgo += 540; // 9시간이 역전되는 버그를 막는 임시 방편소스
      }

      var result = {
        raw:
          d.getFullYear() +
          "-" +
          (d.getMonth() + 1 > 9 ? "" : "0") +
          (d.getMonth() + 1) +
          "-" +
          (d.getDate() > 9 ? "" : "0") +
          d.getDate() +
          " " +
          (d.getHours() > 9 ? "" : "0") +
          d.getHours() +
          ":" +
          (d.getMinutes() > 9 ? "" : "0") +
          d.getMinutes() +
          ":" +
          (d.getSeconds() > 9 ? "" : "0") +
          d.getSeconds(),
        formatted: ""
      };


      if (minsAgo < 60) {
        // 1시간 내(ex: 1분 전, 3분 전, 45분 전)
        result.formatted = minsAgo + "분 전";
      } else if (minsAgo < 60 * 24) {
        // 하루 내(ex: 1시간 전, 5시간 전, 22시간 전)
        result.formatted = Math.floor(minsAgo / 60) + "시간 전";
      } else if (60 * 24 <= minsAgo < 60 * 24 * 7) {
        // 일주일 내(ex: 1일 전, 4일 전 6일 전)
        result.formatted = Math.floor(minsAgo / 60 / 24) + "일 전";
      } else if (c.getFullYear == d.getFullYear) {
        // 올해 안(ex: 8월 4일, 9월 11일)
        result.formatted = d.getMonth() + 1 + "월 " + d.getDate() + "일";
      } else {
        // 올해 밖(ex: 2021-04-21, 2021-08-11)
        result.formatted =
          d.getFullYear() +
          "-" +
          (d.getMonth() + 1 > 9 ? "" : "0") +
          (d.getMonth() + 1) +
          "-" +
          (d.getDate() > 9 ? "" : "0") +
          d.getDate();
      }
      return result.formatted;
    }
  }
};
</script>

<style scoped>
.letter {
  position: relative;
}

.type-team {
  position: absolute;
  width: 12px;
  top: 0;
  bottom: 0;
  left: 0;
  background-color: #38ada9;
}

.type-member {
  position: absolute;
  width: 12px;
  top: 0;
  bottom: 0;
  left: 0;
  background-color: #706fd3;
}
</style>
