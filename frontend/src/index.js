/*******************************************************************************
 * Copyright (c) 2019, 2020 Obeo.
 * This program and the accompanying materials
 * are made available under the erms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
import React, { StrictMode } from 'react';
import ReactDOM from 'react-dom';
import { BrowserRouter } from 'react-router-dom';

import { ApolloProvider } from '@apollo/client';
import { GraphQLClient, graphQLHttpClient, graphQLWebSocketClient } from '@eclipse-sirius/sirius-components';
import CssBaseline from '@material-ui/core/CssBaseline';
import purple from '@material-ui/core/colors/purple';
import { createMuiTheme, ThemeProvider } from '@material-ui/core/styles';
import { ApolloGraphQLClient } from 'ApolloGraphQLClient';
import { Main } from 'main/Main';

import styles from './index.module.css';

import './fonts.css';
import './reset.css';
import './variables.css';
import './Sprotty.css';

const theme = createMuiTheme({
  palette: {
    type: 'light',
    primary: purple
  }
});

ReactDOM.render(
  <ApolloProvider client={ApolloGraphQLClient}>
    <GraphQLClient.Provider value={{ graphQLHttpClient, graphQLWebSocketClient }}>
      <BrowserRouter>
        <ThemeProvider theme={theme}>
          <StrictMode>
            <CssBaseline />
            <div className={styles.app}>
              <Main />
            </div>
          </StrictMode>
        </ThemeProvider>
      </BrowserRouter>
    </GraphQLClient.Provider>
  </ApolloProvider>,
  document.getElementById('root')
);
