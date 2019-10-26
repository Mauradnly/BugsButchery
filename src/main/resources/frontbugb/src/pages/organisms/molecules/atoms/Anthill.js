import React from 'react';


class Anthill extends React.Component{
  render() {
    return (
      <div>
        
        <p>Entrez le nom du territoire que vous voulez désigner comme votre fourmiliere : </p>
        <p>Votre fourmilière restera secrete et apparaîtra comme n'importe quel terrain aux yeux des autres jouers. Néanmoins, votre fourmilière est primordiale à votre survie dans le jeu, si elle se fait prendre par un autre joueur, vous perdez la partie.</p>
        <input type="text" onChange={this.props.handleChangeAnthill} value={this.state.changeAnthill}></input>
        
      </div>
    )
  }
}

export default Anthill;