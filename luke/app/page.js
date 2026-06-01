import Image from "next/image";
import styles from "./page.module.css";
import LoginBttn from "./components/ui/login";
import SignupBttn from "./components/ui/signup";

export default function Home() {
  return (<>
    <header>
      <p>
    <h1>HackEase</h1> <br /> <h3>PARTICIPATE IN HACKATHONS AND MANAGE THEM EFFORTLESSLY</h3>
      </p>
    <LoginBttn /> <br />
    <SignupBttn />
  </header>

    <main> <br />

      <div id="featured">
        <h2>featured</h2>
      </div>

      <div id="cardContainer">
        <Card />
      </div>
  </main>

  </>
  );
}
